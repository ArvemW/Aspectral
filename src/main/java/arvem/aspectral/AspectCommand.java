package arvem.aspectral;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class AspectCommand extends CommandBase {

    public AspectCommand() {
        super("aspect", "Sets an entity's aspect.");
        this.setPermissionGroup(GameMode.Creative);
        //TODO: Add arguments and functionality
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("This command doesn't do anything. Go fix it already!"));
    }
}