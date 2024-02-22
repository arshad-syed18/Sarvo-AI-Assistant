from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline
from langchain_community.llms.huggingface_pipeline import HuggingFacePipeline
from langchain.prompts import PromptTemplate
from langchain.memory import ConversationSummaryMemory, ChatMessageHistory
from transformers.utils import logging

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

def count_tokens(conversation_history):
    total_tokens = sum(len(tokenizer.encode(message["content"])) for message in conversation_history)
    return total_tokens

# Function to capture keyboard input and add to conversational history
def capture_input():
    input_text = input("User: ")
    conversation_history.append({"role": "user", "content": input_text})
    conversation_history.append({"role": "assistant", "content": ""})
    # Check if the user wants to exit
    if "exit" in input_text.lower() or "quit" in input_text.lower():
        print("Goodbye!")
        raise SystemExit


# Start program by asking for initial input from user.
capture_input()

# Limit maximum iterations for conversation
while True:
    

    # Convert conversational history into chat template and tokenize
    inputs = tokenizer.apply_chat_template(conversation_history, return_tensors="pt", return_attention_mask=False).to('cuda')

    # Generate output
    generated_ids = model.generate(inputs,
         max_new_tokens=2048,
         do_sample=True,# more creative
         top_p=0.95,# creativity level p & k
         top_k=40,
         repetition_penalty=1.1,# reduce repitition
         pad_token_id=tokenizer.eos_token_id,# supress warning
         temperature=0.7# randomness
    )
    # Get complete output from model including input prompt
    output = tokenizer.batch_decode(generated_ids)[0]
    
    # Filter only new output information using '</s>' delimiter, then strip starting and trailing whitespace
    output_filtered = output.split('</s>')[-2].strip()
    print("Assistant: "+output_filtered)

    # Update conversation history with the latest output
    conversation_history[-1]["content"] = output_filtered
    
    # Counting total tokens, if exceeds the input limit of model, remove the first message the user and assistant sent to keep size in check
    total_tokens = count_tokens(conversation_history)
    print("tokens: "+str(total_tokens))
    if total_tokens > 4096:
        del conversation_history[2:4]    
        total_tokens = count_tokens(conversation_history)
        print("New total tokens after removal:", total_tokens)

    capture_input()
