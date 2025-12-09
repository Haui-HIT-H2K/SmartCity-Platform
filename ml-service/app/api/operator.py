# Copyright 2025 Haui.HIT - H2K
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Operator API for human-in-the-loop labeling
"""
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional

router = APIRouter(prefix="/operator", tags=["operator"])

class LabelRequest(BaseModel):
    operator_label: str
    notes: Optional[str] = None

@router.get("/unknown/pending")
async def get_pending_unknown():
    """Get events needing operator review"""
    from app.main import unknown_db
    
    if unknown_db is None:
        raise HTTPException(status_code=503, detail="Unknown events database not initialized")
    
    events = unknown_db.get_pending_events(limit=100)
    
    return {
        "success": True,
        "count": len(events),
        "events": events
    }

@router.post("/unknown/{event_id}/label")
async def label_event(event_id: int, request: LabelRequest):
    """Operator labels an unknown event"""
    from app.main import unknown_db
    
    if unknown_db is None:
        raise HTTPException(status_code=503, detail="Unknown events database not initialized")
    
    try:
        unknown_db.label_event(event_id, request.operator_label, request.notes)
        return {
            "success": True,
            "message": f"Event {event_id} labeled as '{request.operator_label}'"
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/stats")
async def get_operator_stats():
    """Get dashboard statistics"""
    from app.main import unknown_db
    
    if unknown_db is None:
        raise HTTPException(status_code=503, detail="Unknown events database not initialized")
    
    stats = unknown_db.get_statistics()
    
    return {
        "success": True,
        "stats": stats
    }

@router.get("/training/status")
async def get_training_status():
    """Get status of labeled events for training"""
    from app.main import unknown_db
    
    if unknown_db is None:
        raise HTTPException(status_code=503, detail="Unknown events database not initialized")
    
    status = {}
    for metric_type in ["temperature", "humidity", "co2"]:
        labeled_data = unknown_db.get_labeled_for_training(metric_type)
        status[metric_type] = len(labeled_data)
    
    return {
        "success": True,
        "labeled_counts": status,
        "retrain_threshold": 100
    }
