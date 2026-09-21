package onecardgame;

/**
 * Constants class for game messages and debug output
 */
public final class GameMessages {
    private GameMessages() {
        // Private constructor to prevent instantiation
    }

    // Card matching messages
    public static final String CARDS_DO_NOT_MATCH = "DEBUG: Cards do not match.";
    public static final String CARDS_MATCH = "DEBUG: Cards match.";
    public static final String JOKER_ALWAYS_MATCHES = "DEBUG: Joker always matches.";
    
    // Card playability messages
    public static final String CHECKING_CARD_PLAYABLE = "DEBUG: Checking if card is playable - Card: %s, Last Card: %s";
    public static final String IS_INITIAL_TURN = "DEBUG: Is Initial Turn: %s";
    public static final String ANY_CARD_PLAYABLE = "DEBUG: Any card is playable as no card is on the table.";
    public static final String NORMAL_MOVE_VALID = "DEBUG: Normal move is valid.";
    
    // Attack blocking messages
    public static final String ATTACK_CARD_BLOCKS = "DEBUG: Attack card blocks another attack card.";
    public static final String ACE_BLOCKS_ACE = "DEBUG: Ace blocks another Ace with the same shape.";
    public static final String JOKER_BLOCKS_ATTACK = "DEBUG: Joker blocks any attack.";
    public static final String DEFENDED = "Defended! Attack blocked.";
    public static final String DEFEND_FAILED = "Defend failed. The card cannot protect from Attack.";
    
    // Accumulated draws messages
    public static final String ACCUMULATED_DRAWS_INCREASED = "DEBUG: Accumulated draws increased by %d";
    
    // Card skipping messages
    public static final String SKIPPING_CARD = "DEBUG: Skipping card %s due to: %s";
    
    // Invalid move messages
    public static final String INVALID_MOVE_CANNOT_PROTECT = "Invalid move. The card can't protect from Attack.";
    public static final String INVALID_MOVE_CARD_DOES_NOT_MATCH = "Invalid move. Card must match either the shape (suit) or number of the last card.";
    public static final String INVALID_MOVE_CARD_CANNOT_BE_PLAYED = "This card cannot be played.";
    
    // Debug messages for invalid moves
    public static final String DEBUG_CARD_CANNOT_PROTECT = "DEBUG: Card cannot protect from Attack.";
    public static final String DEBUG_CARD_DOES_NOT_MATCH = "DEBUG: Card does not match the last played card.";
    public static final String DEBUG_INVALID_MOVE = "DEBUG: %s";
    
    // Game initialization messages
    public static final String CANNOT_START_GAME = "Cannot start game: %s";
    
    // Player input messages
    public static final String SELECT_CARD_OR_DRAW = "Select a card index to play or type '0' to draw: ";
    public static final String SELECT_CARD_INDEX = "Select a card index to play: ";
    
    // Player action messages
    public static final String PLAYER_DREW_ACCUMULATED_CARDS = "You drew all accumulated cards: %d cards.";
    public static final String PLAYER_DREW_CARD = "You drew a card and skipped your turn.";
    public static final String PLAYER_PLAYED_CARD = "You played: %s";
    public static final String ACCUMULATED_PUNISHMENT = "Accumulated punishment: %d cards.";
    public static final String PLAYER_UNDER_ATTACK = "You are under attack! You must defend with an attack card or draw %d cards.";
    public static final String PLAYER_DEFENSE_OPTIONS_HEADER = "Defending cards in your hand:";
    public static final String PLAYER_NO_DEFENSE_CARDS = "You don't have any playable attackable cards to defend. Drawing cards automatically.";
    public static final String FACECARD_EFFECT_APPLIED = "FaceCard effect applied. You can play another card of the same shape.";
    public static final String NUMSEVEN_SHAPE_CHANGE = "7 card played! Shape changed to: %s";
    public static final String SELECT_NEW_SHAPE = "Select a new shape (1=Hearts, 2=Diamonds, 3=Clubs, 4=Spades): ";
    public static final String INVALID_SHAPE_SELECTION = "Invalid shape selection. Please choose 1, 2, 3, or 4.";
    
    // AI action messages
    public static final String AI_TURN = "AI's turn...";
    public static final String AI_DREW_ACCUMULATED_CARDS = "AI decided to draw all accumulated cards: %d cards.";
    public static final String AI_DREW_ALL_CARDS = "AI drew all cards and skipped its turn.";
    public static final String AI_DREW_CARD = "AI drew a card and skipped its turn.";
    public static final String AI_PLAYED_CARD = "AI played: %s";
    public static final String AI_ATTACK = "AI attack! Accumulated draws by attack cards: %d cards.";
    public static final String AI_PLAYED_ANOTHER_CARD = "AI played another card of the same shape: %s";
    public static final String AI_NO_ADDITIONAL_CARD = "AI has no additional card to play of the same shape.";
    public static final String AI_TURN_OVER = "AI's turn is over.";
    public static final String AI_BLOCKED_JOKER = "AI blocked the Joker with its own Joker!";
    
    // Deck messages
    public static final String DECK_EMPTY = "The deck is empty!";
    public static final String DECK_EMPTY_NO_MORE_CARDS = "The deck is empty. No more cards can be drawn.";
    public static final String DECK_EMPTY_NO_CARD_DRAWN = "The deck is empty. No card drawn.";
    
    // Card effect messages
    public static final String ACE_CARD_PLAYED = "Ace card (%s) played! Blocks an attack or initiates an attack for 3 cards!";
    public static final String ATTACK_CARD_PLAYED = "Attack card played! Opponent must draw %d cards unless blocked.";
    public static final String JOKER_PLAYED = "Joker played! Opponent must draw 5 cards unless blocked by another Joker.";
    public static final String FACECARD_PLAYED = "Face card (%s) played! You can play another card of the same shape.";
    
    // Error messages
    public static final String INVALID_INPUT_NUMBER_OR_DRAW = "Invalid input. Please enter a valid number or type '0' to draw.";
    public static final String INVALID_INDEX_RANGE = "Invalid index. Please select a card index between 1 and %d";
    public static final String INVALID_CARD_INDEX = "Invalid card index.";
    public static final String INVALID_INPUT_NUMBER = "Invalid input. Please enter a valid number.";
    public static final String INVALID_INPUT_CARD_INDEX_OR_DRAW = "Invalid input. Please enter a card index or '0' to draw.";
    public static final String INVALID_MOVE_CARD_DOES_NOT_MATCH_MSG = "Invalid move. Card does not match.";
    public static final String PLAYER_HAND_EMPTY = "The player hand is empty!";
    
    // Game state messages
    public static final String PLAYERS_HAND_EMPTY = "Player's hand is empty.\n";
    public static final String PLAYERS_HAND = "Player's hand:\n";
    public static final String AI_HAND_NOT_INITIALIZED = "AI's hand is not initialized.\n";
    public static final String AI_HAS_CARDS = "AI has %d cards.\n";
    public static final String GAME_OVER = "Game Over!";
    public static final String PLAYERS_FINAL_HAND = "Player's final hand:\n";
    public static final String PLAYER_WON_NO_CARDS = "Player won, they have no cards left.\n";
    public static final String PLAYER_HAS_NO_CARDS = "Player has no cards left.";
    public static final String PLAYER_LOST_TOO_MANY_CARDS = "You reached the maximum card limit and lost.\n";
    public static final String AI_FINAL_HAND = "AI's final hand:\n";
    public static final String AI_WON_NO_CARDS = "AI won, they have no cards left.\n";
    public static final String AI_HAS_NO_CARDS = "AI has no cards left.";
    public static final String AI_LOST_TOO_MANY_CARDS = "AI reached the maximum card limit and lost.\n";
    public static final String END_OF_GAME = "End of game.";
    public static final String FINAL_HANDS_WRITTEN_TO_FILE = "Final hands written to file: %s";
    public static final String ERROR_WRITING_TO_FILE = "An error occurred while writing to the file: %s";
    public static final String NOT_ENOUGH_CARDS_TO_DEAL = "Not enough cards in the deck to deal initial hands.";
    public static final String DECK_EMPTY_DURING_DEAL = "Deck became empty during initial deal.";
    public static final String INITIAL_CARD_ON_TABLE = "The initial card on the table is: %s";
    public static final String CURRENT_CARD_ON_TABLE = "Current card on the table: %s";
    
    
    // Error messages
    public static final String INVALID_CARD_INDEX_ERROR = "Invalid card index.";
    public static final String INVALID_CARD_SHAPE_ERROR = "Invalid card shape.";
    public static final String INVALID_CARD_NULL_ERROR = "Card cannot be null.";
    public static final String INVALID_SCANNER_NULL_ERROR = "Scanner cannot be null.";
    
    // Card effect messages
    public static final String NORMAL_CARD_PLAYED = "Normal card (%s) played. No special effect.";
    
    // Display methods
    /**
     * Displays a message to the console.
     * 
     * @param message The message to display
     */
    public static void display(final String message) {
        System.out.println(message);
    }
    
    /**
     * Displays a formatted message to the console.
     * 
     * @param format The format string
     * @param args The arguments to format
     */
    public static void displayFormatted(final String format, final Object... args) {
        System.out.println(String.format(format, args));
    }
    
    /**
     * Displays a prompt message (without newline) to the console.
     * 
     * @param prompt The prompt message to display
     */
    public static void displayPrompt(final String prompt) {
        System.out.print(prompt);
    }
    
    /**
     * Displays an error message to the error console.
     * 
     * @param message The error message to display
     */
    public static void displayError(final String message) {
        System.err.println(message);
    }
    
    /**
     * Displays a formatted error message to the error console.
     * 
     * @param format The format string
     * @param args The arguments to format
     */
    public static void displayErrorFormatted(final String format, final Object... args) {
        System.err.println(String.format(format, args));
    }
}
