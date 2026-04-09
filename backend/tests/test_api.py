from fastapi.testclient import TestClient

def test_root(client: TestClient):
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Welcome to SplitEasy API"}

def test_create_user(client: TestClient):
    response = client.post("/users/", json={
        "uid": "test_user_1",
        "name": "Test User",
        "email": "test@example.com"
    })
    
    if response.status_code != 200:
        with open('users_debug.txt', 'w') as f:
            f.write(response.text)
    assert response.status_code == 200
    data = response.json()
    assert data["uid"] == "test_user_1"
    assert data["name"] == "Test User"
    
def test_create_group(client: TestClient):
    # Mock user dependency
    client.post("/users/", json={"uid": "mock_uid", "name": "Mock User", "email": "mock@test.com"})
    
    response = client.post("/groups/", json={
        "group_name": "Test Group"
    }, headers={"Authorization": "Bearer mock_uid"}) 
    
    if response.status_code != 200:
        with open('groups_debug.txt', 'w') as f:
            f.write(response.text)
    assert response.status_code == 200
    data = response.json()
    assert data["group_name"] == "Test Group"
    assert "group_id" in data
