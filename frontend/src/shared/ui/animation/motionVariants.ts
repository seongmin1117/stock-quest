import { Variants } from 'framer-motion';

// Duration constants for consistent timing
export const DURATION = {
  fastest: 0.1,
  faster: 0.15,
  fast: 0.2,
  normal: 0.3,
  slow: 0.5,
  slower: 0.7,
  slowest: 1.0,
} as const;

// Easing configurations for different interaction types
export const EASING = {
  // Standard easing for most UI interactions
  standard: [0.22, 1, 0.36, 1],
  // Anticipatory easing for hover states
  anticipate: [0.68, -0.55, 0.265, 1.55],
  // Sharp easing for immediate feedback
  sharp: [0.4, 0, 0.2, 1],
  // Smooth easing for gentle transitions
  smooth: [0.25, 0.46, 0.45, 0.94],
  // Bounce easing for playful interactions
  bounce: [0.68, -0.55, 0.265, 1.55],
} as const;

// Page transition variants
export const pageVariants: Variants = {
  initial: {
    opacity: 0,
    y: 20,
    scale: 0.98,
  },
  enter: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
  exit: {
    opacity: 0,
    y: -20,
    scale: 0.98,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// Card animation variants
export const cardVariants: Variants = {
  initial: {
    opacity: 0,
    y: 30,
    scale: 0.9,
  },
  animate: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
  hover: {
    y: -4,
    scale: 1.02,
    transition: {
      duration: DURATION.fast,
      ease: EASING.anticipate,
    },
  },
  tap: {
    scale: 0.98,
    transition: {
      duration: DURATION.fastest,
      ease: EASING.sharp,
    },
  },
  exit: {
    opacity: 0,
    y: -30,
    scale: 0.9,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// Button interaction variants
export const buttonVariants: Variants = {
  initial: {
    scale: 1,
  },
  hover: {
    scale: 1.05,
    transition: {
      duration: DURATION.fast,
      ease: EASING.anticipate,
    },
  },
  tap: {
    scale: 0.95,
    transition: {
      duration: DURATION.fastest,
      ease: EASING.sharp,
    },
  },
};

// Trading-specific animations
export const priceChangeVariants: Variants = {
  neutral: {
    color: '#666666',
    scale: 1,
  },
  increase: {
    color: '#4caf50',
    scale: [1, 1.1, 1],
    transition: {
      duration: DURATION.normal,
      ease: EASING.bounce,
    },
  },
  decrease: {
    color: '#f44336',
    scale: [1, 1.1, 1],
    transition: {
      duration: DURATION.normal,
      ease: EASING.bounce,
    },
  },
};

// Portfolio value animation variants
export const portfolioValueVariants: Variants = {
  initial: {
    scale: 1,
    opacity: 1,
  },
  updating: {
    scale: [1, 1.05, 1],
    opacity: [1, 0.8, 1],
    transition: {
      duration: DURATION.slow,
      ease: EASING.smooth,
      repeat: 0,
    },
  },
  profit: {
    color: '#4caf50',
    textShadow: '0 0 8px rgba(76, 175, 80, 0.3)',
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
  loss: {
    color: '#f44336',
    textShadow: '0 0 8px rgba(244, 67, 54, 0.3)',
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
};

// Order status animation variants
export const orderStatusVariants: Variants = {
  pending: {
    opacity: [0.5, 1, 0.5],
    scale: [1, 1.02, 1],
    transition: {
      duration: DURATION.slower,
      ease: EASING.smooth,
      repeat: Infinity,
    },
  },
  filled: {
    scale: [1, 1.1, 1],
    color: '#4caf50',
    transition: {
      duration: DURATION.normal,
      ease: EASING.bounce,
    },
  },
  cancelled: {
    opacity: [1, 0.3],
    color: '#f44336',
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
  rejected: {
    x: [-5, 5, -5, 5, 0],
    color: '#f44336',
    transition: {
      duration: DURATION.normal,
      ease: EASING.sharp,
    },
  },
};

// Modal animation variants
export const modalVariants: Variants = {
  initial: {
    opacity: 0,
    scale: 0.8,
    y: 50,
  },
  animate: {
    opacity: 1,
    scale: 1,
    y: 0,
    transition: {
      duration: DURATION.normal,
      ease: EASING.anticipate,
    },
  },
  exit: {
    opacity: 0,
    scale: 0.8,
    y: 50,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// Backdrop animation variants
export const backdropVariants: Variants = {
  initial: {
    opacity: 0,
  },
  animate: {
    opacity: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
  exit: {
    opacity: 0,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// List item stagger animation
export const listContainerVariants: Variants = {
  initial: {
    opacity: 0,
  },
  animate: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.1,
    },
  },
  exit: {
    opacity: 0,
    transition: {
      staggerChildren: 0.05,
      staggerDirection: -1,
    },
  },
};

export const listItemVariants: Variants = {
  initial: {
    opacity: 0,
    y: 20,
    scale: 0.9,
  },
  animate: {
    opacity: 1,
    y: 0,
    scale: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.standard,
    },
  },
  exit: {
    opacity: 0,
    y: -20,
    scale: 0.9,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// Notification animation variants
export const notificationVariants: Variants = {
  initial: {
    opacity: 0,
    x: 300,
    scale: 0.8,
  },
  animate: {
    opacity: 1,
    x: 0,
    scale: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.anticipate,
    },
  },
  exit: {
    opacity: 0,
    x: 300,
    scale: 0.8,
    transition: {
      duration: DURATION.fast,
      ease: EASING.sharp,
    },
  },
};

// Chart data point animation
export const chartPointVariants: Variants = {
  initial: {
    scale: 0,
    opacity: 0,
  },
  animate: {
    scale: 1,
    opacity: 1,
    transition: {
      duration: DURATION.normal,
      ease: EASING.bounce,
    },
  },
  hover: {
    scale: 1.2,
    transition: {
      duration: DURATION.fast,
      ease: EASING.anticipate,
    },
  },
};

// Loading spinner variants
export const spinnerVariants: Variants = {
  animate: {
    rotate: 360,
    transition: {
      duration: 1,
      ease: 'linear',
      repeat: Infinity,
    },
  },
};

// Progress bar variants
export const progressBarVariants: Variants = {
  initial: {
    scaleX: 0,
    originX: 0,
  },
  animate: (progress: number) => ({
    scaleX: progress / 100,
    transition: {
      duration: DURATION.slow,
      ease: EASING.standard,
    },
  }),
};

// Pulse animation for real-time indicators
export const pulseVariants: Variants = {
  animate: {
    scale: [1, 1.2, 1],
    opacity: [1, 0.7, 1],
    transition: {
      duration: DURATION.slower,
      ease: EASING.smooth,
      repeat: Infinity,
    },
  },
};

// Slide in from different directions
export const slideVariants = {
  left: {
    initial: { x: -100, opacity: 0 },
    animate: { x: 0, opacity: 1 },
    exit: { x: -100, opacity: 0 },
  },
  right: {
    initial: { x: 100, opacity: 0 },
    animate: { x: 0, opacity: 1 },
    exit: { x: 100, opacity: 0 },
  },
  up: {
    initial: { y: 100, opacity: 0 },
    animate: { y: 0, opacity: 1 },
    exit: { y: 100, opacity: 0 },
  },
  down: {
    initial: { y: -100, opacity: 0 },
    animate: { y: 0, opacity: 1 },
    exit: { y: -100, opacity: 0 },
  },
} as const;