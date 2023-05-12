# Agency

Agency is a framework that aims to simplify building applications on top of a Large Language Model (LLM) in Java.  It
provides a number of features that enable this:

- Tools to create robust prompts for the LLM interactions (with history, variable injection, and examples for multi-shot)
- The ability to add "skills" to agents to enable local automated actions upon LLM response commands
- Integration with OpenAI embeddings functionality to vectorize text by generating embeddings
- Integration with Milvus vector DB to provide contextual / similarity searches
- Loaders for different types of files / documents

...and maybe some more.  NOTE: Agency is under active development:

1. None of the APIs are finalized.  Once the library starts to solidify and we understand the right levels of abstraction
for generically interacting with LLMs, the services will be genericized as interfaces that can be implemented by different
LLM APIs.  Same for vector stores, etc.  We will only support concrete implementations of the OpenAI API and the Milvus API
at this point as we iterate on initial goals.

2. This was inspired by [langchain](https://python.langchain.com/en/latest/index.html), but is not a port or reimplementation. That
said, I really like what they've done so far.  Unfortunately, I don't know Python and Java has gotten pretty nice
over the years.

## Contributing
 
 Pull requests welcome!  I think in particular once the API has solidified a bit and refactoring has made the structure sane, the 
 primary needs will be documentation, new agents, and new skills.
 
 - Discord user support: [agency-user](https://discord.com/channels/1102704021342519368/1102704226561441832)
 - Discord developer discussion: [agency-dev](https://discord.com/channels/1102704021342519368/1102704258471710783)
 
