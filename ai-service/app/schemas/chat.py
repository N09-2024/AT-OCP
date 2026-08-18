from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any


class ChatRequest(BaseModel):
    message: str
    conversationId: Optional[str] = None
    atContext: Optional[Dict[str, Any]] = None


class ChatResponse(BaseModel):
    answer: str
    sources: List[str] = Field(default_factory=list)
    confidence: str = "HIGH"
    suggestedQuestions: List[str] = Field(default_factory=list)
