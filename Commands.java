import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
/*
    Class for responding to received messages and executing appropriate commands.
    @Author: Jacob Gnatz
    @Date: 2019-11-22
 */
public class Commands extends ListenerAdapter {
    //Instance Variables
    private final String ID = "638867894431907850";
    private final String PREFIX = "/";
    private final String HELP = "help";
    private final String HEAD = "head";
    private final String[] COMMANDS = new String[] {HELP, HEAD};
    private User prevAuth;

    //Constructor
    public Commands()
    {
        //Other instance variables are preset and final
        prevAuth = null;
    }

    //Methods
    /*
        Main function for determining response to received messages.
        @param event: The event contents of the received message.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //If prevAuth is null, no previous messages
        if (prevAuth == null)
            prevAuth = event.getAuthor();

        //Prevent bot loops
        if (event.getAuthor().isBot())
            return;

        //Store common event variables
        Message msg = event.getMessage();
        String content = event.getMessage().getContentRaw();

        //Print to System
        System.out.println("Message from " + event.getAuthor().getName()
                + ". Contents: " + event.getMessage().getContentRaw());

        /*Message decoding*/
        //Check for mention, call mention function
        if (!msg.getMentionedUsers().isEmpty())
        {
            for (User user: msg.getMentionedUsers())
            {
                if (user.getId().equals(ID)) {
                    //Bot is mentioned
                    onMention(event);
                    break;
                }
            }
        }
        //Check for prefix, execute valid commands
        else if(content.startsWith(PREFIX))
        {
            /*ADD MORE KEYWORDS HERE*/
            if(content.startsWith(HELP, 1))
                onHelp(event);
            else if (content.startsWith(HEAD, 1))
                onHead(event);
            else
                //No valid command entered
                System.out.println("Invalid command received!");
        }
        else
            //No mention or prefix found
            System.out.println("No instructions received!");
        //Update prevAuth to this author for next event
        prevAuth = event.getAuthor();
    }

    /*
        Method for helping users utilize CraftCrab on first use.
     */
    public void onMention(MessageReceivedEvent event)
    {
        String prepend = "Hello fellow \uD83E\uDD80!\n" +
                "To use my commands, type `/` proceeded by a command.";
        String sendStr = commandBuilder(event, prepend);

        event.getChannel().sendMessage(sendStr).queue();
    }

    /*
        Method for helping users remember commands and explain their usage (if provided).
     */
    public void onHelp(MessageReceivedEvent event)
    {
        //Split message into args
        String[] args = splitIntoArgs(event);

        //Check if user has provided any arguments
        if (args.length > 1)
        {
            //Check for command keywords, print associated messages
            /*ADD MORE KEYWORDS HERE*/
            if (args[1].equals(HELP))
                event.getChannel().sendMessage(explainHelp()).queue();
            else if (args[1].equals(HEAD))
                event.getChannel().sendMessage(explainHead()).queue();
            else
                //No valid keyword entered
                event.getChannel().sendMessage(":x: `" + args[1] + "` is not a valid command!").queue();
        }
        //User has provided no arguments, print help message
        else
        {
            String sendStr;
            String prepend = "If you want help on a specific command, " +
                    "type `/help` proceeded by the command.";
            sendStr = commandBuilder(event, prepend);
            event.getChannel().sendMessage(sendStr).queue();
        }
    }

    /*
        Internal method for documenting the help function.
        @return: String that doesn't really explain the help function. 'Cause you're a bastard.
     */
    private String explainHelp()
    {
        return "Go suck a \uD83E\uDD9E. No infinite loops for you!";
    }

    /*
        Method for splitting a give command from https://minecraft-heads.com/ into usable NBT
        data for an in-game data parser. Prints a message with instructions and extracted
        data components.
     */
    public void onHead(MessageReceivedEvent event)
    {
        //Split message into args
        String[] args = splitIntoArgs(event);

        //Check if user has provided any args
        if (args.length > 1)
        {
            //Check if arguments match a give playerhead command
            if (checkHeadArguments(event))
            {
                //All arguments and keywords present, print decoupled message
                event.getChannel().sendMessage(sendHeadMessage(event)).queue();
            }
            //Arguments do not match a give playerhead command, print help message
            else
            {
                event.getChannel().sendMessage(":x: Incorrect command given!\nType `/help head` for usage.").queue();
            }
        }
        //User has provided no arguments, print help message
        else
        {
            event.getChannel().sendMessage(":x: No command given!\nType `/help head` for usage.").queue();
        }
    }

    /*
        Internal method for checking user's provided arguments for the head command.
        Note: This method will break if Mojang changes their give command formatting or NBT data formatting.
        @return: Boolean validating this commands arguments.
     */
    private boolean checkHeadArguments(MessageReceivedEvent event)
    {
        //Validation info
        String give = "/give";
        String atP = "@p";
        String playerhead = "minecraft:player_head";

        String display = "display";
        String name = "Name";
        String skullowner = "SkullOwner";
        String id = "Id";
        String properties = "Properties";
        String textures = "textures";
        String value = "Value";

        String[] keywords = new String[] {display, name, skullowner, id, properties, textures, value};

        //Split message into args, message
        String[] args = splitIntoArgs(event);
        String msg = event.getMessage().getContentRaw();

        //Validate initial args
        if (args[1].equals(give) &&
            args[2].equals(atP) &&
            args[3].startsWith(playerhead))
        {
            //Validate message contains keywords
            for (String keyword : keywords)
            {
                if (!(msg.contains(keyword)))
                {
                    //Failed to find essential keyword
                    System.out.println("checkHeadArguments: Did not find keyword: " + keyword);
                    return false;
                }
            }
            //All keywords found, return true
            return true;
        }
        //Invalid initial args
        else
        {
            System.out.println("checkHeadArguments: Invalid initial args!");
            return false;
        }
    }

    /*
        Internal method for extracting key values from a give command for formatting later.
        Assumes checkHeadArguments() returned true.
        Note: This method will break if Mojang changes their give command formatting or NBT data formatting.
        @return: String array with key values.
     */
    private String[] decoupleHeadArguments(MessageReceivedEvent event)
    {
        //Extract and modify give command message
        String cmd = event.getMessage().getContentRaw();
        cmd = cmd.substring(cmd.indexOf("minecraft:"));
        String[] keyValues = cmd.split(",");

        //Extract and modify display name
        keyValues[0] = keyValues[0].substring(keyValues[0].indexOf("display:{Name:\"")+15, keyValues[0].indexOf("}") + 1);
        keyValues[0] = keyValues[0].replaceAll("\\\\", "");
        //Extract and modify skullowner ID
        keyValues[1] = keyValues[1].substring(keyValues[1].indexOf("SkullOwner:Id:\"")+16);
        keyValues[1] = keyValues[1].replaceAll("\"", "");
        //Extract and modify texture value
        keyValues[2] = keyValues[2].substring(keyValues[2].indexOf("Value:\"")+7, keyValues[2].indexOf("}"));
        keyValues[2] = keyValues[2].replaceAll("\"", "");

        //Return keyValues
        return keyValues;
    }

    /*
        Internal method for formatting a message for player usage from key values.
        @return: String of the formatted keyvalues and basic instructions.
     */
    private String sendHeadMessage(String[] keyValues, MessageReceivedEvent event)
    {
        int pageCount = 1;
        String sendStr = "Hello <@" + event.getAuthor().getId() + ">! Here are your page values:\n" +
                "**Page " + pageCount + ":** *The name of your block and the link you got it from, in whatever convention you choose.*\n" +
                "**Page " + ++pageCount + ":** " + keyValues[0] + "\n" +
                "**Page " + ++pageCount + ":** " + keyValues[1] + "\n" +
                "**Page " + ++pageCount + ":** " + keyValues[2] + "\n";
        return sendStr;
    }

    /*
        Public method for returning the formatted head message.
        @return: String of the formatted keyvalues and basic instructions.
     */
    public String sendHeadMessage(MessageReceivedEvent event)
    {
        return sendHeadMessage(decoupleHeadArguments(event), event);
    }

    /*
        Internal method for documenting the head function.
        @return: String that explains to the user the head function.
     */
    private String explainHead()
    {
        return "`/head (give command)`\n" +
                "**Description:** Returns a message with the values necessary to generate a playerhead based on the given playerhead command.\n\n" +
                "**How to use:** Go to https://minecraft-heads.com/custom-heads and browse for a head decoration you would like to use.\n" +
                "Click on the head and copy the `Give-Code` field.\n" +
                "(Note: you must copy the entire code within the field.)\n" +
                "Type `/head` proceeded by your copied code.\n" +
                "A message will be then be returned containing the exact contents of each page for your Book & Quill.\n\n" +
                "Questions? See #player-heads for more info.";
    }

    private String[] splitIntoArgs(MessageReceivedEvent event)
    {
        return event.getMessage().getContentRaw().split(" ");
    }

    /*
        Internal method for building the list of commands this class currently implements.
        @return: String representing a list of current commands.
     */
    private String commandBuilder(MessageReceivedEvent event)
    {
        StringBuilder reStrB = new StringBuilder();
        reStrB.append("Current commands are: ");
        for (String cmd : COMMANDS)
        {
            reStrB.append("`" + cmd + "`, ");
        }
        reStrB.replace(reStrB.length()-2, reStrB.length(), ".");
        String reStr = reStrB.toString();
        return reStr;
    }

    /*
        Internal method for building a list of commands this class currently implements.
        Includes a prepended message to the list in a previous line.
        @return: String of current commands with a prepended message.
     */
    private String commandBuilder(MessageReceivedEvent event, String prepend)
    {
        return prepend + "\n" + (commandBuilder(event));
    }
}
