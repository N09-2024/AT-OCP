import unittest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


class TestHealth(unittest.TestCase):
    def test_health_endpoint(self):
        response = client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "UP")
        self.assertIn("version", data)
        self.assertIn("llm_provider", data)


if __name__ == "__main__":
    unittest.main()
