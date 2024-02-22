import requests

# Define the URL of your FastAPI server
url = "http://localhost:8000/user-input/"

# Function to send a user input and receive assistant response
def send_user_input(user_input):
    # Send a POST request to the API endpoint with the user input
    response = requests.post(url, params={"user_input": user_input})

    # Check if the request was successful (status code 200)
    if response.status_code == 200:
        # Get the JSON response containing the assistant's response
        data = response.json()
        assistant_response = data["assistant_response"]
        return assistant_response
    else:
        print("Error:", response.text)
        return None

# Loop for chatting with the assistant
while True:
    # Get user input
    user_input = input("You: ")

    # Send user input to the assistant and get the response
    assistant_response = send_user_input(user_input)

    # Print assistant response
    if assistant_response:
        print("Assistant:", assistant_response)
    
    # Check for exit command
    if user_input.lower() == "exit":
        print("Goodbye!")
        break
