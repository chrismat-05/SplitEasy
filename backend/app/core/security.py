from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import User
import os
import firebase_admin
from firebase_admin import credentials, auth

security = HTTPBearer()

# Initialize Firebase Admin if credentials are provided
firebase_cred_path = os.environ.get("FIREBASE_CREDENTIALS_PATH")
if firebase_cred_path and os.path.exists(firebase_cred_path) and not firebase_admin._apps:
    cred = credentials.Certificate(firebase_cred_path)
    firebase_admin.initialize_app(cred)
elif not firebase_admin._apps and os.environ.get("MOCK_AUTH", "False").lower() != "true":
    # Just initialize a default app for environment-based credentials if deployed (e.g. GCP)
    try:
        firebase_admin.initialize_app()
    except Exception:
        pass

def get_current_user(http_credentials: HTTPAuthorizationCredentials = Depends(security), db: Session = Depends(get_db)):
    """
    Validates Firebase JWT.
    Falls back to mock UUID authentication if MOCK_AUTH is enabled.
    """
    token = http_credentials.credentials
    mock_auth = os.environ.get("MOCK_AUTH", "False").lower() == "true"
    
    uid = None
    if mock_auth:
        uid = token
    else:
        try:
            decoded_token = auth.verify_id_token(token)
            uid = decoded_token.get("uid")
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Invalid authentication credentials: {str(e)}",
            )
            
    if not uid:
         raise HTTPException(
             status_code=status.HTTP_401_UNAUTHORIZED,
             detail="Invalid authentication credentials",
         )

    user = db.query(User).filter(User.uid == uid).first()
    if not user:
         raise HTTPException(
             status_code=status.HTTP_401_UNAUTHORIZED,
             detail="User profile not registered internally",
         )
    return user
