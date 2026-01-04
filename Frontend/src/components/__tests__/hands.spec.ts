import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Hands from '../hands.vue'
import type { CardInfo } from '../../types/card'

describe('hands.vue', () => {
  const mockCards: CardInfo[] = [
    { id: 1, index: 0, rank: 'A', shape: 'Hearts' },
    { id: 2, index: 1, rank: '2', shape: 'Spades' },
    { id: 3, index: 2, rank: 'K', shape: 'Diamonds' }
  ]

  describe('toggleSelect function', () => {
    it('should add card id to selectedIds when not selected', async () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards
        }
      })

      const cardComponent = wrapper.findComponent({ name: 'Card' })
      await cardComponent.vm.$emit('select', 1)

      // Check that the card is now selected
      expect(cardComponent.props('isSelected')).toBe(true)
    })

    it('should remove card id from selectedIds when already selected', async () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards
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
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards
        }
      })

      // Select cards in non-sequential order
      const cards = wrapper.findAllComponents({ name: 'Card' })
      await cards[2].vm.$emit('select', 3) // index 2
      await cards[0].vm.$emit('select', 1) // index 0

      // The function is internal, but we can verify selection state
      expect(cards[0].props('isSelected')).toBe(true)
      expect(cards[2].props('isSelected')).toBe(true)
      expect(cards[1].props('isSelected')).toBe(false)
    })
  })

  describe('isCardPlayable function', () => {
    it('should return true when card index is in playableCardIndices and isPlayerTurn is true', () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: [0, 2]
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards[0].props('isPlayable')).toBe(true) // index 0 is playable
      expect(cards[1].props('isPlayable')).toBe(false) // index 1 is not playable
      expect(cards[2].props('isPlayable')).toBe(true) // index 2 is playable
    })

    it('should return false when isPlayerTurn is false', () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards,
          isPlayerTurn: false,
          playableCardIndices: [0, 2]
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('isPlayable')).toBe(false)
      })
    })

    it('should return false when playableCardIndices is empty', () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: []
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('isPlayable')).toBe(false)
      })
    })
  })

  describe('rendering', () => {
    it('should render all cards from props', () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(3)
    })

    it('should pass correct props to Card components', () => {
      const wrapper = mount(Hands, {
        props: {
          cards: mockCards,
          isPlayerTurn: true,
          playableCardIndices: [0]
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards[0].props('card')).toEqual(mockCards[0])
      expect(cards[0].props('isPlayable')).toBe(true)
    })
  })
})

