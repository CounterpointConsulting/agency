# Agency

It might be easier to explain what agency is NOT.  It was originally intended as a Java framework for building applications on top of LLMs (like gpt-3.5-turbo and gpt-4 from OpenAI or models from HuggingFace).  The feature list included:

- Tools to create robust prompts for the LLM interactions (with history, variable injection, and examples for multi-shot)
- The ability to add "skills" to agents to enable local automated actions upon LLM response commands
- Integration with OpenAI embeddings functionality to vectorize text by generating embeddings
- Integration with Milvus vector DB to provide contextual / similarity searches
- Loaders for different types of files / documents

...and maybe some more.  It still *has* all those things, and in fact you could use it as a library *for* those things.  The focus has changed fairly radically, however.

The focus now is on building a small, focused core of agents run by a "planner" to achieve tasks.  Ultimately, these agents and their "skills" will be fully hot-loadable and built dynamically by the agents themselves.  So, if while responding to a user's request, the Planner decides its current set of agents doesn't suffice to meet the current goal, it will create a new agent on the fly.  That the Planner can generate a set of steps to solve many problems is already established.  That the models can write functional Java and Python code is established.  There's no reason why it can't fill the gaps on the fly, assuming the correct level of detail can be provided.

NOTE: Agency is under active development:

1. None of the APIs are finalized.  Once the library starts to solidify and we understand the right levels of abstraction
for generically interacting with LLMs, the services will be genericized as interfaces that can be implemented by different
LLM APIs.  Same for vector stores, etc.  We will only support concrete implementations of the OpenAI API and the Milvus API
at this point as we iterate on initial goals.

2. This was inspired by [langchain](https://python.langchain.com/en/latest/index.html), but is not a port or reimplementation. That
said, I really like what they've done so far.  Unfortunately, I don't know Python and Java has gotten pretty nice
over the years.

3. I just found out [Semantic Kernel](https://learn.microsoft.com/en-us/semantic-kernel/overview/) uses some of the same terminology.  We have a greater global presence, however, so our users likely won't be confused.  I suspect the major search engines will give our usage preference.

4. I've shifted focus away from trying to build a generic application framework, and instead focus on creating an agent system to augment LLMs, but let the LLM itself build the agents.  Sounds confusing, and it might be.    


# Updates
2023-07-17: Agency development is shifting gears after some reflection, moving towards a focus on giving LLMs the ability to create dynamic agents.  That is, instead of providing the model with a set of agents that it can use to solve problems, leverage the ability of the LLM to both generate plans and write code to give it the ability to write new agents on the fly.  It will be an application you can run instead of a framework (of course you could treat it as a framework and code up your own agents to augment the core ones) and watch it grow!

2023-06-28: I've been reorganizing the conceptual layers and formalizing some of the interfaces.  The biggest change is inserting a proxy/planner layer that selects and coordinates the agents.  The basic flow looks something like:

1. User sends message to the Planner
2. Planner creates a multi-step plan to respond to the user or service their request, where each step in the plan can be completed by an Agent using one or more of that Agent's Skills
3. Planner attempts to complete each step in the plan by sending the designated Agent a message describing the action(s) that need to be taken.
4. Once the last step is completed, the Planner gives the final response to the user.

This approach (as described above it's 4 steps, in reality it's more like 9) removes manual chaining from the responsibility of the coder, since assuming the instructions are clear and your agents have the requisite skills, the Planner should be able to figure out the chaining on its own.  This is a working theory, we'll see.

There are cases where I could see manually wanting to define the steps yourself (you being the coder) for cases like having a model generate multiple ideas to work with, or having multiple Agents vote on some question.  As I was thinking about it, though, there's no reason the Planner couldn't coordinate that also.

The above also led to the realization that you really want multiple conversations going on at the same time -- for instance, there's no reason to keep the messages between the Planner and its agents in the context of the conversation between the user and the Planner.  So, if you're smart about how you build your conversation history you can save a whole lot of unneeded tokens.

A couple of random things to think through currently:
1. The Planner can send a response message to the user
2. An agent can send a response message (usually a request for more information) to the user
3. An agent may need to bail out and tell the Planner: a) I can't do that; b) Bad request / data; c) I was communicating with them and the plan probably has to change
4. Can an agent / would an agent need or want to call another agent?  Is an agent / does an agent need its own Planner?  Can a Planner's plan involve spawning a new Planner?

Visualizing:

Case 1 (simple plan):
-
User -> Planner -> Agent(s) -> Planner -> User

Case 2 (with agent->user sub-conversation)
-
User -> Planner -> \[Agent -> User -> Agent (loop as needed)\] -> Planner -> User

Case 3 (with error)
-
User -> Planner -> Agent -> User -> Agent (bad things happen) -> Planner -> \[Agent or User\]

An alternate to having Agents able to communicate back to the User directly, the Agent could respond to the Planner and the Planner message the user, then immediately route the next message to the same agent...  seems more complicated.  Unless I find some reason it makes sense, I'll leave that particular sleeping dog alone.

I just walked my dog, and now I'm pretty sure a Planner is just an agent, nothing more, nothing less.  Maybe to the Planner, the user IS JUST ANOTHER AGENT.

Parting thought...  

These things are really good at programming now.  They could certainly write their own Agents and Skills.  I know there are Java libraries for dynamically loading plugins, but maybe each Agent is just a container deployed in k8s waiting to respond, or maybe each in a JVM listening to a Kafka queue or something...  there is no reason a new Agent couldn't be built and dynamically added to the system while it's running, all by... the system.

## Contributing
 
 Pull requests welcome!  I think in particular once the API has solidified a bit and refactoring has made the structure sane, the 
 primary needs will be documentation, new agents, and new skills.
 
 - Discord user support: [agency-user](https://discord.com/channels/1102704021342519368/1102704226561441832)
 - Discord developer discussion: [agency-dev](https://discord.com/channels/1102704021342519368/1102704258471710783)
 
