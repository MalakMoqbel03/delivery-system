import sys
import os
import requests
import json

"""
this file called by Java to get AI responses
Java runs this script using ProcessBuilder
Python sends request to AI and returns the result
"""
def load_api_key():
    key_paths = [
        "api_key.txt",
        "../api_key.txt",
        os.path.join(os.path.dirname(__file__), "..", "api_key.txt")
    ]
    for path in key_paths:
        if os.path.exists(path):
            with open(path, "r") as f:
                return f.read().strip()

    print("ERROR: api_key.txt not found")
    sys.exit(1)

# Build prompt based on query type
def build_prompt(query_type, location_name, location_type, details):
    if query_type == "insights":
        return (
            f"You are a delivery logistics assistant. "
            f"Give exactly 3 short bullet points of delivery advice for:\n"
            f"Location: {location_name}\n"
            f"Type: {location_type}\n"
            f"Priority: {details}\n"
            f"Focus on: best delivery time, potential challenges, and one practical tip."
        )

    elif query_type == "restocking":
        return (
            f"You are a warehouse manager assistant. "
            f"The warehouse '{location_name}' has stock {details} (current/max).\n"
            f"Give exactly 3 bullet points:\n"
            f"1. Should we send more stock now?\n"
            f"2. What product types are likely in demand?\n"
            f"3. Suggested restocking schedule."
        )

    elif query_type == "comparison":
        parts = details.split("|")
        if len(parts) >= 6:
            n1, p1, d1, n2, p2, d2 = parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
        else:
            n1, p1, d1, n2, p2, d2 = "Location 1", "MEDIUM", "10", "Location 2", "LOW", "20"

        return (
            f"You are a delivery scheduler. Compare these two delivery points:\n"
            f"1. {n1} — Priority: {p1}, Distance: {d1} km\n"
            f"2. {n2} — Priority: {p2}, Distance: {d2} km\n"
            f"Which should be delivered first and why? Give a 3-line answer."
        )

    elif query_type == "route":
        return (
            f"You are a route planning assistant. "
            f"Here are the delivery stops: {details}\n"
            f"Suggest:\n"
            f"1. The most efficient order to visit them\n"
            f"2. A good break point\n"
            f"3. Whether all stops fit in an 8-hour shift"
        )

    else:
        return f"Give one short delivery insight for: {location_name}"


def call_openai(api_key, prompt):
    url = "https://api.openai.com/v1/chat/completions"

    body = {
        "model": "gpt-3.5-turbo",
        "messages": [
            {"role": "user", "content": prompt}
        ],
        "max_tokens": 300,
        "temperature": 0.7
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    response = requests.post(url, headers=headers, data=json.dumps(body), timeout=30)

    if response.status_code != 200:
        print(f"ERROR: OpenAI API returned status {response.status_code}")
        print(response.text)
        sys.exit(1)

    result = response.json()
    try:
        text = result["choices"][0]["message"]["content"]
        return text.strip()
    except (KeyError, IndexError) as e:
        print(f"ERROR: Unexpected OpenAI response format: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 5:
        print("Usage: python3 ai_query.py <query_type> <location_name> <location_type> <details>")
        sys.exit(1)

    query_type = sys.argv[1]
    location_name = sys.argv[2]
    location_type = sys.argv[3]
    details = sys.argv[4]
    api_key = load_api_key()
    prompt = build_prompt(query_type, location_name, location_type, details)
    result = call_openai(api_key, prompt)
    print(result)