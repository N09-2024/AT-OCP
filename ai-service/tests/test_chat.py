import unittest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


class TestChat(unittest.TestCase):
    def test_chat_endpoint(self):
        payload = {
            "message": "Quelles sont les mesures obligatoires pour un travail en hauteur ?",
            "atContext": {
                "atId": "AT-2026-001",
                "description": "Changement de vanne en hauteur",
            },
        }
        response = client.post("/api/ai/chat", json=payload)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("answer", data)
        self.assertIn("sources", data)
        self.assertTrue(len(data["sources"]) > 0)
        self.assertIn("suggestedQuestions", data)


if __name__ == "__main__":
    unittest.main()
