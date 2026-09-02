"""
Tests de conformité au plan d'architecture IA :
- Connecteur SQL PostgreSQL (lecture seule) - couche LangChain "[Connecteurs SQL]"
- Mémoire conversationnelle - couche LangChain "[Gestion Mémoire]"
- CrewAI : agents "Agent Analyste Risques" et "Agent Inspecteur HSE" conformes au plan
"""

import pytest

from app.sql.connector import _validate_readonly, run_readonly_query, is_sql_enabled
from app.memory.chat_memory import ChatMemoryManager


class TestConnecteurSql:
    def test_select_simple_accepte_et_limite_ajoutee(self):
        sql = _validate_readonly("SELECT nom_risque FROM risque")
        assert sql.startswith("SELECT")
        assert "LIMIT 20" in sql

    def test_limit_existant_non_duplique(self):
        sql = _validate_readonly("SELECT nom FROM permis LIMIT 5")
        assert sql.count("LIMIT") == 1

    def test_ecriture_refusee(self):
        for bad in [
            "DELETE FROM risque",
            "UPDATE risque SET nom_risque = 'x'",
            "INSERT INTO risque VALUES (1)",
            "DROP TABLE risque",
        ]:
            with pytest.raises(ValueError):
                _validate_readonly(bad)

    def test_multi_instructions_refusees(self):
        with pytest.raises(ValueError):
            _validate_readonly("SELECT 1; DROP TABLE risque")

    def test_table_non_referentielle_refusee(self):
        with pytest.raises(ValueError):
            _validate_readonly("SELECT email FROM utilisateurs")

    def test_table_autorisee_acceptee(self):
        sql = _validate_readonly(
            "SELECT numero, objet, statut FROM autorisations_travail ORDER BY date_creation DESC"
        )
        assert "autorisations_travail" in sql

    def test_sans_base_reponse_claire(self):
        # Sans DATABASE_URL, l'outil répond proprement (pas d'exception).
        if not is_sql_enabled():
            out = run_readonly_query("SELECT nom_risque FROM risque")
            assert "indisponible" in out.lower()


class TestMemoireConversation:
    def test_echange_sauvegarde_et_restitution(self):
        manager = ChatMemoryManager()
        manager.save_exchange("conv-1", "Quels EPI pour la hauteur ?", "Le harnais...")
        history = manager.get_history_text("conv-1")
        assert "Utilisateur" in history
        assert "Quels EPI pour la hauteur" in history
        assert "harnais" in history

    def test_conversation_inconnue_vide(self):
        manager = ChatMemoryManager()
        assert "aucun historique" in manager.get_history_text("inconnue").lower()
        assert "aucun historique" in manager.get_history_text(None).lower()

    def test_fenetrage_k_echanges(self):
        manager = ChatMemoryManager(window_k=2)
        for i in range(5):
            manager.save_exchange("conv-2", f"question {i}", f"reponse {i}")
        history = manager.get_history_text("conv-2")
        assert "question 4" in history
        assert "question 0" not in history

    def test_clear(self):
        manager = ChatMemoryManager()
        manager.save_exchange("conv-3", "q", "a")
        manager.clear("conv-3")
        assert "aucun historique" in manager.get_history_text("conv-3").lower()


class TestAgentsConformitePlan:
    """Les agents doivent reprendre les libellés du plan d'architecture."""

    def test_agent_analyste_risques(self):
        from app.agents.agent_risques import build_agent_risques
        agent = build_agent_risques()
        assert agent.role.startswith("Agent Analyste Risques")
        tool_names = [t.name for t in agent.tools]
        assert "query_ocp_database" in tool_names

    def test_agent_inspecteur_hse(self):
        from app.agents.agent_hse import build_agent_hse
        agent = build_agent_hse()
        assert agent.role.startswith("Agent Inspecteur HSE")
        tool_names = [t.name for t in agent.tools]
        assert "query_ocp_database" in tool_names
        assert "verify_hse_rules" in tool_names

    def test_agents_crew_principal(self):
        from app.agents.risk_agent import build_risk_agent
        from app.agents.hse_agent import build_hse_agent
        from app.agents.at_agent import build_at_agent
        assert build_risk_agent().role.startswith("Agent Analyste Risques")
        assert build_hse_agent().role.startswith("Agent Inspecteur HSE")
        assert build_at_agent() is not None

    def test_crew_2_agents_analyse_intervention(self):
        from app.crew import run_crew_analyse_intervention  # noqa: F401 - import utilisable
        import inspect as pyinspect
        from app import crew as crew_mod
        src = pyinspect.getsource(crew_mod.run_crew_analyse_intervention)
        # Le duo du plan est bien orchestré (Risques → HSE avec contexte partagé)
        assert "build_agent_risques" in src
        assert "build_agent_hse" in src
        assert "context=[task_risques]" in src
