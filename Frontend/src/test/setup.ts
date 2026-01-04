import { vi } from 'vitest'

// Mock AudioContext as a class constructor
class MockAudioContext {
  createOscillator() {
    return {
      connect: vi.fn(),
      start: vi.fn(),
      stop: vi.fn(),
      frequency: { value: 0 },
      type: ''
    }
  }
  createGain() {
    return {
      connect: vi.fn(),
      gain: {
        setValueAtTime: vi.fn(),
        exponentialRampToValueAtTime: vi.fn()
      }
    }
  }
  destination = {}
  currentTime = 0
}

globalThis.AudioContext = MockAudioContext as any
globalThis.webkitAudioContext = MockAudioContext as any

// Mock HTMLAudioElement methods that aren't implemented in jsdom
Object.defineProperty(HTMLAudioElement.prototype, 'load', {
  value: vi.fn(),
  writable: true,
  configurable: true
})

Object.defineProperty(HTMLAudioElement.prototype, 'play', {
  value: vi.fn().mockResolvedValue(undefined),
  writable: true,
  configurable: true
})

// Mock fetch globally
globalThis.fetch = vi.fn() as any

