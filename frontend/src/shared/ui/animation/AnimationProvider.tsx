'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { MotionConfig } from 'framer-motion';

interface AnimationContextValue {
  reduceMotion: boolean;
  animationSpeed: number;
  enableAnimations: boolean;
  setAnimationSpeed: (speed: number) => void;
  setEnableAnimations: (enabled: boolean) => void;
}

interface AnimationProviderProps {
  children: ReactNode;
  defaultAnimationSpeed?: number;
  defaultEnableAnimations?: boolean;
}

const AnimationContext = createContext<AnimationContextValue | undefined>(undefined);

export function AnimationProvider({
  children,
  defaultAnimationSpeed = 1,
  defaultEnableAnimations = true,
}: AnimationProviderProps) {
  const [reduceMotion, setReduceMotion] = useState(false);
  const [animationSpeed, setAnimationSpeed] = useState(defaultAnimationSpeed);
  const [enableAnimations, setEnableAnimations] = useState(defaultEnableAnimations);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
      setReduceMotion(mediaQuery.matches);

      const handleChange = (e: MediaQueryListEvent) => {
        setReduceMotion(e.matches);
        if (e.matches) {
          setEnableAnimations(false);
        }
      };

      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }
  }, []);

  const shouldAnimate = enableAnimations && !reduceMotion;

  const contextValue: AnimationContextValue = {
    reduceMotion,
    animationSpeed,
    enableAnimations: shouldAnimate,
    setAnimationSpeed,
    setEnableAnimations,
  };

  return (
    <AnimationContext.Provider value={contextValue}>
      <MotionConfig
        reducedMotion={reduceMotion ? 'always' : 'never'}
        transition={{
          duration: shouldAnimate ? 0.3 / animationSpeed : 0,
          ease: [0.22, 1, 0.36, 1], // Custom easing for professional feel
        }}
      >
        {children}
      </MotionConfig>
    </AnimationContext.Provider>
  );
}

export function useAnimation() {
  const context = useContext(AnimationContext);
  if (context === undefined) {
    throw new Error('useAnimation must be used within an AnimationProvider');
  }
  return context;
}

export { AnimationContext };