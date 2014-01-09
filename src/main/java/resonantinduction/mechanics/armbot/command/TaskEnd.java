package resonantinduction.mechanics.armbot.command;

import resonantinduction.mechanics.armbot.TaskBase;

/** @author DarkGuardsman */
public class TaskEnd extends TaskBase
{
	public TaskEnd()
	{
		super("end", TaskType.END);
	}

	@Override
	public TaskBase clone()
	{
		return new TaskEnd();
	}
}