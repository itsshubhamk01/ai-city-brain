/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        base: '#0B1120',
        surface: '#111A2E',
        raised: '#16213A',
        hairline: '#25314D',
        ink: {
          DEFAULT: '#E7ECF7',
          muted: '#8B98B8',
          faint: '#5A6685',
        },
        signal: {
          nominal: '#3DDC97',
          watch: '#F5A623',
          critical: '#FF5D5D',
        },
        accent: {
          DEFAULT: '#5B8DEF',
          soft: '#8FB0F5',
        },
        agent: {
          flood: '#22D3EE',
          traffic: '#F5A623',
          emergency: '#FF5D5D',
          waste: '#A3E635',
          energy: '#C084FC',
          healthcare: '#FB7185',
          brain: '#5B8DEF',
        },
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      boxShadow: {
        panel: '0 1px 0 0 rgba(255,255,255,0.03) inset, 0 8px 24px -12px rgba(0,0,0,0.6)',
        glow: '0 0 0 1px rgba(91,141,239,0.4), 0 0 24px -4px rgba(91,141,239,0.5)',
      },
      keyframes: {
        sweep: {
          '0%': { transform: 'rotate(-90deg)', opacity: '0' },
          '10%': { opacity: '1' },
          '100%': { transform: 'rotate(90deg)', opacity: '0' },
        },
        pulseDot: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.35' },
        },
      },
      animation: {
        sweep: 'sweep 1.6s ease-out 1',
        'pulse-dot': 'pulseDot 2s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
