# Agency

Agency is an application built on top of Large Language Models (LLMs) like GPT-4.  

The basic idea is this: you give agency a goal, it develops a "plan" to accomplish the goal and executes the plan (if it determines it can).  Each step in the plan must be accomplished by using a "skill".  Plans and skills, that's the whole idea.  

"But GPT can't actually DO anything," you say.  This is true, but it can write code to do things.  Those are "skills".

"But GPT can't execute code," you once again complain.  That is also true, but YOUR computer can execute code written by GPT.  

Putting those pieces together, the outcome will be a system that can augment its own capabilities autonomously and serve the user's needs more capably than one with pre-determined, "hard-coded" functionality.  Given the nature of the systems underpinnings, however, it's also possible (and perhaps likely) that it will spin wildly out of control in its attempts.  Let's see what happens!

## Some Details

The above explanation is very hand-wavy, but hopefully it gives some high-level impression of the reason agency could be interesting.  

To get a bit more concrete, the basic top-level loop looks like this:

* User submits a request or goal
* Behind the scenes, agency builds a prompt that gets sent to OpenAI.  The prompt includes the user's request AND the list of existing "skills" which can be used in accomplishing the steps of the plan GPT comes up with
* GPT returns (to agency, the user still does not see this) a plan, formatted in a known JSON schema.  The plan is an array of steps.  Each step is a formatted request to be sent to an HTTP endpoint.  If GPT is unable to develop a plan given the available skills, there are two possible ways to proceed.  Either agency informs the user (hopefully with a descriptive reason) it is unable to accomplish the requested goal OR it begins the process of developing a new skill that can carry the plan a step closer to achieving the goal.
* If a new skill is to be developed, a few things happen (still transparent to the user):
  * On the local filesystem, a folder is created and populated with the skeleton of a Javascript microservice
  * The model generates the Javascript code for the skill, which is written to a placeholder in the mocroservice project's code
  * The Docker container is built, tagged, and started, with port 80 internal to the container is mapped to a random port on the host (invocations of this skill will be sent to this port)
* If GPT is able to develop a plan using the available skills, it will carry out the HTTP call to invoke each step's skill invocation (synchronously, in order)

Sometimes it works, sometimes hilarity ensues.


## Contributing
 
 Pull requests welcome!  I think in particular once the API has solidified a bit and refactoring has made the structure sane, the 
 primary needs will be documentation, new agents, and new skills.
 
 - Discord user support: [agency-user](https://discord.com/channels/1102704021342519368/1102704226561441832)
 - Discord developer discussion: [agency-dev](https://discord.com/channels/1102704021342519368/1102704258471710783)
 
