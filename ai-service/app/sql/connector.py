"""
Connecteur SQL PostgreSQL pour la couche LangChain (cf. architecture cible :
LangChain = [Gestion Mémoire] [Transformations Prompt] [Connecteurs SQL]).

Accès LECTURE SEULE aux référentiels OCP (tables du backend Spring Boot) :
- SELECT uniquement, LIMIT forcé, une seule instruction.
- La connexion est créée paresseusement : si DATABASE_URL est absente ou si
  PostgreSQL est injoignable, les agents reçoivent un message clair et le
  référentiel statique (app/referentiel.py) reste la source de repli.
"""

import logging
import re
from typing import Optional

from app.config.settings import settings

logger = logging.getLogger(__name__)

# Tables métier exposées aux agents (référentiels + AT en lecture).
TABLES_AUTORISEES = {
    "zone", "zones",
    "service", "services",
    "equipement", "equipements",
    "entreprise_externe", "entreprises_externes",
    "risque", "risques",
    "mesure_preparation", "mesures_preparation",
    "epi", "epis",
    "moyen_acces", "moyens_acces",
    "type_permis", "types_permis",
    "autorisations_travail",
    "permis",
}

engine = None  # SQLAlchemy Engine (créé au premier usage)


def _build_url() -> Optional[str]:
    """URL SQLAlchemy depuis DATABASE_URL, ou assemblée depuis les vars DB_*."""
    url = getattr(settings, "DATABASE_URL", None)
    if url:
        if url.startswith("postgresql://"):
            url = url.replace("postgresql://", "postgresql+psycopg2://", 1)
        return url
    host = getattr(settings, "DB_HOST", None)
    if not host:
        return None
    return (
        f"postgresql+psycopg2://{settings.DB_USER}:{settings.DB_PASSWORD}"
        f"@{host}:{settings.DB_PORT}/{settings.DB_NAME}"
    )


def _get_engine():
    global engine
    if engine is not None:
        return engine
    try:
        from sqlalchemy import create_engine
        url = _build_url()
        if not url:
            logger.info("Connecteur SQL désactivé : DATABASE_URL / DB_* non configurés.")
            return None
        engine = create_engine(url, pool_pre_ping=True, pool_size=2, max_overflow=2)
        logger.info("Connecteur SQL initialisé (%s:%s/%s).",
                    settings.DB_HOST or "url", settings.DB_PORT, settings.DB_NAME or "db")
        return engine
    except Exception as ex:
        logger.warning("Initialisation du connecteur SQL impossible : %s", ex)
        return None


def is_sql_enabled() -> bool:
    return _get_engine() is not None


_TABLE_RE = re.compile(r"\bFROM\s+([a-zA-Z_][a-zA-Z0-9_\.\"]*)", re.IGNORECASE)


def _validate_readonly(query: str) -> str:
    """Garantit SELECT simple sur tables référentielles, avec LIMIT."""
    q = (query or "").strip().rstrip(";").strip()
    lowered = q.lower()
    forbidden = ("insert", "update", "delete", "drop", "alter", "create",
                 "truncate", "grant", "revoke", "copy", "merge", "call")
    if not lowered.startswith("select"):
        raise ValueError("Seules les requêtes SELECT sont autorisées.")
    if ";" in q:
        raise ValueError("Une seule instruction SQL est autorisée.")
    if any(re.search(rf"\b{kw}\b", lowered) for kw in forbidden):
        raise ValueError("Mot-clé d'écriture interdit : lecture seule.")
    # Tables touchées (FROM uniquement : les sous-requêtes FROM sont couvertes aussi)
    for m in _TABLE_RE.finditer(q):
        table = m.group(1).strip('"').split(".")[-1].lower()
        if table not in TABLES_AUTORISEES:
            raise ValueError(f"Table '{table}' non autorisée (référentiels AT uniquement).")
    if not re.search(r"\blimit\b", lowered):
        q += " LIMIT 20"
    return q


def run_readonly_query(query: str, max_rows: int = 20) -> str:
    """Exécute une requête SELECT en lecture seule ; retourne un tableau texte."""
    eng = _get_engine()
    if eng is None:
        return ("Connecteur SQL indisponible (DATABASE_URL non configuré). "
                "Utiliser le référentiel statique fourni dans le prompt.")
    try:
        sql = _validate_readonly(query)
        from sqlalchemy import text
        with eng.connect() as conn:
            result = conn.execute(text(sql))
            rows = result.fetchmany(max_rows)
            columns = list(result.keys())
        if not rows:
            return "Requête exécutée : aucun résultat."
        header = " | ".join(columns)
        lines = [header, "-" * len(header)]
        lines += [" | ".join(str(v) for v in row) for row in rows]
        if len(rows) == max_rows:
            lines.append(f"... (limité à {max_rows} lignes)")
        return "\n".join(lines)
    except ValueError as ve:
        return f"Requête refusée : {ve}"
    except Exception as ex:
        logger.warning("Erreur SQL (lecture seule) : %s", ex)
        return f"Erreur d'exécution SQL : {ex}"


def list_available_tables() -> str:
    """Liste les tables référentielles réellement présentes (pour les prompts d'agents)."""
    eng = _get_engine()
    if eng is None:
        return "Connecteur SQL indisponible."
    try:
        from sqlalchemy import inspect
        insp = inspect(eng)
        present = sorted(
            t for t in insp.get_table_names()
            if t.lower() in TABLES_AUTORISEES
        )
        return ", ".join(present) if present else "Aucune table référentielle trouvée."
    except Exception as ex:
        return f"Inspection impossible : {ex}"
