import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PlayerPanel from '../PlayerPanel.vue'
import type { CardInfo } from '../../types/card'

describe('PlayerPanel.vue', () => {
  const mockCards: CardInfo[] = [
    { id: 1, index: 0, rank: 'A', shape: 'Hearts' },
    { id: 2, index: 1, rank: '2', shape: 'Spades' },
    { id: 3, index: 2, rank: 'K', shape: 'Diamonds' }
  ]

  describe('toggleSelect function', () => {
    it('should add card id to selectedIds when not selected', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: true
        }
      })

      const cardComponent = wrapper.findComponent({ name: 'Card' })
      await cardComponent.vm.$emit('select', 1)

      // Check that the card is now selected
      expect(cardComponent.props('isSelected')).toBe(true)
    })

    it('should remove card id from selectedIds when already selected', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: true
        }
      })

      const cardComponent = wrapper.findComponent({ name: 'Card' })
      
      // Select first
      await cardComponent.vm.$emit('select', 1)
      expect(cardComponent.props('isSelected')).toBe(true)
      
      // Deselect
      await cardComponent.vm.$emit('select', 1)
      expect(cardComponent.props('isSelected')).toBe(false)
    })
  })

  describe('getSelectedIndices function', () => {
    it('should return sorted indices of selected cards', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      // Select cards in non-sequential order
      const cards = wrapper.findAllComponents({ name: 'Card' })
      await cards[2].vm.$emit('select', 3) // index 2
      await cards[0].vm.$emit('select', 1) // index 0

      // Trigger play cards
      const playButton = wrapper.find('.play-btn')
      expect(playButton.exists()).toBe(true) // Button should exist when cards are selected
      await playButton.trigger('click')

      expect(wrapper.emitted('playCards')).toBeTruthy()
      expect(wrapper.emitted('playCards')?.[0]).toEqual([[0, 2]]) // Should be sorted
    })

    it('should return empty array when no cards are selected', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: true
        }
      })

      const playButton = wrapper.find('.play-btn')
      expect(playButton.exists()).toBe(false) // Button shouldn't exist when nothing selected
    })
  })

  describe('isCardPlayable function', () => {
    it('should return true when card index is in playableCardIndices and isPlayerTurn is true', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: [0, 2],
          isOwnPanel: true
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards[0].props('isPlayable')).toBe(true) // index 0 is playable
      expect(cards[1].props('isPlayable')).toBe(false) // index 1 is not playable
      expect(cards[2].props('isPlayable')).toBe(true) // index 2 is playable
    })

    it('should return false when isPlayerTurn is false', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: false,
          playableCardIndices: [0, 2],
          isOwnPanel: true
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('isPlayable')).toBe(false)
      })
    })

    it('should return false when playableCardIndices is empty', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: [],
          isOwnPanel: true
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('isPlayable')).toBe(false)
      })
    })
  })

  describe('hasPlayableCards computed', () => {
    it('should return true when playableCardIndices has items and isPlayerTurn is true', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: [0, 1],
          isOwnPanel: true
        }
      })

      // hasPlayableCards is used internally, but we can verify through button visibility
      // Draw button should be visible
      expect(wrapper.find('.draw-btn').exists()).toBe(true)
    })

    it('should return false when isPlayerTurn is false', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: false,
          playableCardIndices: [0, 1],
          isOwnPanel: true
        }
      })

      expect(wrapper.find('.draw-btn').exists()).toBe(false)
    })
  })

  describe('hasOneCard computed', () => {
    it('should return true when cards array has exactly one card', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: [mockCards[0]],
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      expect(wrapper.find('.one-card-btn').exists()).toBe(true)
    })

    it('should return false when cards array has more than one card', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      expect(wrapper.find('.one-card-btn').exists()).toBe(false)
    })
  })

  describe('handleDraw function', () => {
    it('should emit draw event when draw button is clicked', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      const drawButton = wrapper.find('.draw-btn')
      await drawButton.trigger('click')

      expect(wrapper.emitted('draw')).toBeTruthy()
    })
  })

  describe('handleOneCard function', () => {
    it('should emit oneCard event when one card button is clicked', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: [mockCards[0]],
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      const oneCardButton = wrapper.find('.one-card-btn')
      await oneCardButton.trigger('click')

      expect(wrapper.emitted('oneCard')).toBeTruthy()
    })
  })

  describe('handlePlayCards function', () => {
    it('should emit playCards event with selected indices and clear selection', async () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      // Select cards
      const cards = wrapper.findAllComponents({ name: 'Card' })
      await cards[0].vm.$emit('select', 1)
      await cards[2].vm.$emit('select', 3)

      const playButton = wrapper.find('.play-btn')
      expect(playButton.exists()).toBe(true) // Button should exist when cards are selected
      await playButton.trigger('click')

      expect(wrapper.emitted('playCards')).toBeTruthy()
      expect(wrapper.emitted('playCards')?.[0]).toEqual([[0, 2]])
      
      // Selection should be cleared - wait for next tick to allow reactive updates
      await wrapper.vm.$nextTick()
      
      // After clearing, button should not exist if no cards are selected
      const playButtonAfter = wrapper.find('.play-btn')
      expect(playButtonAfter.exists()).toBe(false)
    })

    it('should not emit playCards when no cards are selected', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          isOwnPanel: true
        }
      })

      expect(wrapper.emitted('playCards')).toBeFalsy()
    })
  })

  describe('rendering for other players', () => {
    it('should show card backs when isOwnPanel is false', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: false,
          playerName: 'AI'
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('showBack')).toBe(true)
      })
    })

    it('should not show action buttons when isOwnPanel is false', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: false,
          isPlayerTurn: true
        }
      })

      expect(wrapper.find('.actions').exists()).toBe(false)
    })

    it('should show turn indicator for other players when it is their turn', () => {
      const wrapper = mount(PlayerPanel, {
        props: {
          cards: mockCards,
          isOwnPanel: false,
          isPlayerTurn: true,
          playerName: 'AI'
        }
      })

      expect(wrapper.find('.turn-indicator').exists()).toBe(true)
      expect(wrapper.text()).toContain("AI's Turn")
    })
  })
})

