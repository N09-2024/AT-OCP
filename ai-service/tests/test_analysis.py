import unittest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


class TestAnalysis(unittest.TestCase):
    def test_analyze_at_endpoint(self):
        payload = {
            "atId": "AT-TEST-001",
            "description": "Travaux de soudure sur tuyauterie d'acide en hauteur à 4 mètres",
            "installation": "Atelier Phosphorique",
            "equipement": "Circuit Acide Sulfurique",
            "visiteFaite": True,
            "sectionFRenseignee": True,
        }
        response = client.post("/api/ai/analyze-at", json=payload)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("summary", data)
        self.assertIn("identifiedRisks", data)
        self.assertIn("recommendedMeasures", data)
        self.assertIn("sources", data)
        self.assertTrue(len(data["identifiedRisks"]) > 0)

    def test_legacy_endpoints(self):
        response = client.post("/analyse-intervention", json={"description": "Travaux en espace confiné"})
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("risques", data)
        self.assertIn("mesures", data)


if __name__ == "__main__":
    unittest.main()
