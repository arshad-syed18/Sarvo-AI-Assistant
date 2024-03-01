from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
from langchain_community.llms.huggingface_pipeline import HuggingFacePipeline
from langchain.prompts import PromptTemplate
from langchain.memory import ConversationSummaryMemory, ChatMessageHistory
from transformers.utils import logging
from fastapi import FastAPI
import uvicorn
from pyngrok import ngrok
import requests
from pydantic import BaseModel

# set log for transformers to error only, to remove warnings in terminal
logging.get_logger("transformers").setLevel(logging.ERROR)

# download the model files from huggingface.co and give path here
model_name_or_path = './TheBloke_Mistral-7B-Instruct-v0.2-GPTQ'

print("Loading Model...")
model = AutoModelForCausalLM.from_pretrained(model_name_or_path,
                                             device_map="auto",
                                             trust_remote_code=True,
                                             revision="main")

tokenizer = AutoTokenizer.from_pretrained(model_name_or_path, use_fast=True, trust_remote_code=True, padding_side='left')

# Define conversation history and backstory for assistant, NOTE: change the prompt to add custom user name and backstory as taken from user
conversation_history = [
    {
        "role": "user",
        "content": "Act as a virtual elderly companion assistant for John, a 71 year old war veteran who lives alone, likes cats and dogs."
    },
    {
        "role": "assistant",
        "content": ""
    }
]
model.to('cuda')

app = FastAPI()

def count_tokens(conversation_history):
    total_tokens = sum(len(tokenizer.encode(message["content"])) for message in conversation_history)
    return total_tokens

# Model input
class UserInput(BaseModel):
    input: str

@app.post("/user-input")
async def receive_user_input(user_input: UserInput):
    inputText = user_input.input
    # Add user input to conversation history
    conversation_history.append({"role": "user", "content": inputText})
    conversation_history.append({"role": "assistant", "content": ""})

    # Generate response
    inputs = tokenizer.apply_chat_template(conversation_history, return_tensors="pt", return_attention_mask=False).to('cuda')
    generated_ids = model.generate(inputs,
                                   max_new_tokens=350,
                                   do_sample=True,
                                   top_p=0.95,
                                   top_k=40,
                                   repetition_penalty=1.1,
                                   pad_token_id=tokenizer.eos_token_id,
                                   temperature=0.7)
    output = tokenizer.batch_decode(generated_ids)[0]
    output_filtered = output[output.rfind('</s>') + len('</s>'):].strip()
    # print("output: "+str(output_filtered))

    # Update conversation history
    conversation_history[-1]["content"] = output_filtered

    # Count tokens and remove old messages if necessary
    total_tokens = count_tokens(conversation_history)
    if total_tokens > 2000:
        del conversation_history[2:4]
        total_tokens = count_tokens(conversation_history)

    # Return LLM response
    print("success")
    return {"assistant_response": output_filtered}

# Run below command in terminal to make tunnel
#  ngrok http --domain=deep-friendly-kodiak.ngrok-free.app 8000
#print("Public URL:", ngrok_tunnel.public_url)


# Start FastAPI server

uvicorn.run(app, host="localhost", port=8000)
# --------------------------------------------------------------------------------------------------
