FROM python:3.11-slim

# ─── System deps ──────────────────────────────────────────────────────────────
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        wget \
        curl \
        gcc \
        g++ \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .

# ─── Python deps ──────────────────────────────────────────────────────────────
# Important: On fixe setuptools<70.0.0 pour préserver pkg_resources requis par crewai
RUN pip install --no-cache-dir "setuptools==69.5.1" "wheel"
RUN pip install --no-cache-dir --prefer-binary -r requirements.txt

COPY app ./app
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
