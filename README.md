# One Card Game

This repository contains two ways to play the card game:

- **Vue version:** browser interface with a Spring Boot backend and AI opponent.

## Preview
![alt text](<one card game.gif>)

## Prerequisites

- Java 21+
- Node.js 20.19+ or 22.12+ for the Vue version

## Start the Vue Version

Open two PowerShell terminals from the repository root.

### Terminal 1: backend

```powershell
Set-Location .\Backend
.\mvnw.cmd spring-boot:run
```

The backend runs on `http://localhost:8081`.

### Terminal 2: frontend

```powershell
Set-Location .\Frontend
npm install
npm run dev
```

Open `http://localhost:5173/game` in a browser. The root URL redirects to `/game`.

The backend terminal and frontend terminal must remain open while playing.

## Game Rules

1. The game deals cards to the player and AI and places an initial card on the table.
2. Play a card that matches the last card by suit or rank.
3. A Joker matches any card during normal play.
4. Aces match other Aces regardless of suit, but do not match other ranks by rank.
5. If no playable card is available, draw cards. Drawing ends the current turn.
6. Playing a Jack, Queen, or King gives the same player another turn.
7. Playing a 7 changes the active suit. In the Vue version, choose the suit in the dialog.
8. A 2 starts an attack requiring the next player to draw 2 cards or defend.
9. An Ace starts an attack requiring 3 cards or a valid defense.
10. A Joker starts an attack requiring 5 cards or a valid defense.
11. Attack penalties stack when attack cards are played in succession.
12. Defense cards must have equal or greater attack strength:
    - A 2 can defend against a 2 of the same suit.
    - An Ace can defend against an Ace of any suit, or a 2 of the same suit.
    - A Joker can defend against any attack card.
13. While under attack, only valid attack or defense cards can be played. Otherwise, draw the accumulated penalty.
14. The first player to empty their hand wins.

In the Vue version, select playable cards in your hand and use the play or draw action. The AI turn runs automatically. When one card remains, use the `One Card!` button.

## AI Behavior Logic

The AI makes its own decisions during its turn. Its behavior combines deterministic priorities with probability-based choices so that it does not play identically every time.

### 1. When under attack

- The AI looks for a playable attack or defense card whose punishment value is at least as high as the incoming attack.
- It attempts to defend with a 95% probability when a valid defense is available.
- If it does not defend, or has no valid defense, it draws the accumulated penalty and ends its turn.
- A 2 can defend against a 2 of the same suit, an Ace can defend against an Ace of any suit or a same-suit 2, and a Joker can defend against any attack.

### 2. Choosing a normal play

- If a normal, non-attack card is playable, the AI always prefers to play it.
- If only attack cards are playable, the AI plays one with a 65% probability; otherwise it draws one card.
- A Joker remains wild during normal play and can match any card.

### 3. Playing a 7

The AI considers a playable 7 when changing the active suit is strategically useful:

- 70% chance when the player has fewer than three cards.
- When the player has one or two cards, the AI considers the number of different suits in its hand and usually changes to a suit that benefits its next plays.
- It has a 75% chance to change suit when it has cards across multiple suits.
- It has a 90% chance to keep the current suit when that suit is useful for its remaining hand.
- Otherwise, the AI uses its normal 7-card probabilities: 30% when matching normal cards are available, 60% when matching attack cards are available, and 100% when no other matching option exists.

### 4. Dropping multiple cards

- The AI groups cards with the same rank and may drop a matching group together.
- It always drops a group when it has more than 10 cards.
- With 10 or fewer cards, it drops a group with a 95% probability when attack cards remain in its hand.
- If no attack cards remain after the group, it drops the group with a 90% probability.
- Attack cards are ordered last when possible so the final card preserves the attack effect and accumulated penalty.

### 5. Face cards and Joker behavior

- A Jack, Queen, or King gives the AI another play in the same turn.
- The AI may continue its turn after a face card and can use a Joker afterward because a Joker ignores suit and rank matching.
- When the AI plays a Joker, it may immediately play another Joker to block or continue the attack.

### 6. Drawing

If the AI has no suitable play, or its probability check chooses not to play, it draws one card and ends its turn. When under attack, it draws the full accumulated penalty instead of drawing only one card.

## Project Locations

- `Frontend/` - Vue application
- `Backend/` - Spring Boot API used by Vue
- `Backend/early_work/` - Java command-line card game

## Start the Early Work Version

From the repository root:

```powershell
Set-Location .\Backend\early_work
.\compile-and-run.ps1
```

The script compiles the card-game Java sources and starts `terminal_mode.OneCardGame`.
The generated `.class` files are temporary build files and are automatically deleted when the game exits, including after pressing `Ctrl+C`.

You can also compile the sources directly:

```powershell
Set-Location .\Backend\early_work\src\main\java
$buildDirectory = Join-Path (Get-Location) '.build'
$sourceFiles = Get-ChildItem -Path '.' -Recurse -Filter '*.java' -File |
    Select-Object -ExpandProperty FullName
javac -d $buildDirectory $sourceFiles
java -cp $buildDirectory terminal_mode.OneCardGame
Remove-Item $buildDirectory -Recurse -Force
```
