package com.c20g.labs.agency.skill.planner;

import com.c20g.labs.agency.skill.Skill;
import com.c20g.labs.agency.skill.SkillDescription;
import com.c20g.labs.agency.skill.SkillService;

@SkillService
public class PlannerSkill implements Skill {

    @Override
    public String execute(String jsonRequest) throws Exception {
        try {

            String reqTemplate = """
                The user has made the following request:

                === BEGIN USER REQUEST ===
                {request}
                === END USER REQUEST ===


                === BEGIN DOC REFERENCES ===

                Installing content server is easy!

                === END ====

                Create a step-by-step plan to respond, using the agents available:

                {agents}

                If you cannot complete the request using the available agents, return 
                a description of either a new skill that can be added to an agent or
                a new type of agent and what skills it should have.

                Your response should begin with the word "plan" or "build", followed
                by a newline character.

                If the steps in the plan can all be completed by existing agents and
                their existing skills, the line with "plan" should be followed by the
                following JSON:

                    {
                        "type": "create-plan-response",
                        "steps": [
                            {
                                "step_number": 1,
                                "agent": "ProgrammerBot",
                                "skill": "write_python_script",
                                "description": "Write a python function that adds two numbers"
                            },
                            {
                                "step_number": 2,
                                "agent": "FilesystemBot",
                                "skill": "write_file",
                                "file_content": "lorem ipsum"
                            }
                        ]
                    }

                Do not include anything other than the specified JSON.

                If, while creating the step-by-step plan, it appears none of the existing
                agents have the skill needed to complete a 
                    """;

        }
        catch(Exception e) {

        }
        
        return "";
    }

    @Override
    public SkillDescription describe() throws Exception {
        return new SkillDescription(
            "plan",
            "This will create a step-by-step plan to respond to a user's request.",
            "Creates a step-by-step plan, utilizing one or more agents and their skills.  When you need to use this skill, return the JSON formatted as {\"type\":\"plan\", \"description\" : \"<Description of the users request>\"}"
        );
    }
    
}
