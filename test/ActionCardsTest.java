//import org.junit.Test;
//import java.util.List;
//import static org.junit.Assert.*;
//
//public class ActionCardsTest {
//    @Test
//    public void testPlayableCards() {
//        // Set up the hand and last card
//        List<Card> hand = List.of(
//                new NormalCard(2, "Hearts"),
//                new NormalCard(3, "Spades"),
//                new NormalCard(2, "Diamonds")
//        );
//        Card lastCard = new NormalCard(2, "Clubs");
//
//        // Initialize ActionCards and get playable cards
//        ActionCards actionCards = new ActionCards();
//        List<Card> playableCards = actionCards.getPlayableCards(hand, lastCard, isInitialTurn);
//
//        // Verify the playable cards
//        assertEquals(2, playableCards.size()); // Expect 2 playable cards
//        assertEquals(hand.get(0), playableCards.get(0)); // First playable card
//        assertEquals(hand.get(2), playableCards.get(1)); // Second playable card
//    }
//}
