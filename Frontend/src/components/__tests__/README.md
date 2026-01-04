# Vue Component Unit Tests

This directory contains unit tests for all Vue components in the One Card Game application.

## Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with UI
npm run test:ui

# Run tests with coverage
npm run test:coverage
```

## Test Files

- **Card.spec.ts** - Tests for the Card component
  - `onClick` function behavior
  - Rendering of card front/back
  - CSS class application (selected, playable)

- **Deck.spec.ts** - Tests for the Deck component
  - `deckCards` computed property
  - Card generation based on deckSize
  - Visual card limit (max 5 cards shown)

- **PlayerPanel.spec.ts** - Tests for the PlayerPanel component
  - `toggleSelect` function
  - `getSelectedIndices` function
  - `isCardPlayable` function
  - `hasPlayableCards` computed
  - `hasOneCard` computed
  - `handleDraw`, `handleOneCard`, `handlePlayCards` functions
  - Rendering for own panel vs other players

- **UsedCardPile.spec.ts** - Tests for the UsedCardPile component
  - `cardsToShow` computed property
  - Card stacking logic
  - Fallback to lastUsedCard

- **TurnTransition.spec.ts** - Tests for the TurnTransition component
  - `playBeepSound` function
  - `handleAudioError` function
  - Watch on `show` prop
  - Auto-hide after 2 seconds
  - Rendering for PLAYER vs AI

- **hands.spec.ts** - Tests for the hands component
  - `toggleSelect` function
  - `getSelectedIndices` function
  - `isCardPlayable` function
  - Card rendering

- **GameBoard.spec.ts** - Tests for the GameBoard component
  - `onMounted` hook (setupGame)
  - `handleDraw` function
  - `handleOneCard` function
  - `handlePlayCards` function
  - `refreshState` function
  - `aiHandCards` computed property
  - `onTransitionComplete` function
  - Game state rendering

## API Tests

- **useOneCardGame.spec.ts** - Tests for the useOneCardGame composable
  - `fetchGameState` function
  - `setupGame` function
  - `playAction` function
  - `executeAITurn` function
  - `stepGame` function
  - `isCardPlayable` function
  - Error handling
  - Loading state management

## Test Coverage

All tests aim to cover:
- Function behavior and return values
- Event emissions
- Prop handling
- Computed properties
- Error cases
- Edge cases
- Component rendering

