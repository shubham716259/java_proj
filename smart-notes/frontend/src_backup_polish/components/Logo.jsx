import React from 'react';

/**
 * Logo component rendering the brand SVG icon and optional "Smart Notes" title text.
 * Props:
 *  - size (default 32): width and height of the SVG icon
 *  - showText (default true): whether to display the text label
 */
export default function Logo({ size = 32, showText = true }) {
  return (
    <div className="logo-container">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="60 50 200 200"
        width={size}
        height={size}
        className="logo-icon"
        aria-hidden="true"
      >
        <rect x="60" y="50" width="200" height="200" rx="44" fill="#6366f1" />
        <path d="M100 80H196L220 104V220H100Z" fill="#fff" />
        <path d="M196 80V104H220Z" fill="#c7d2fe" />
        <rect x="118" y="124" width="84" height="8" rx="4" fill="#c7d2fe" />
        <rect x="118" y="146" width="64" height="8" rx="4" fill="#c7d2fe" />
        <rect x="118" y="168" width="76" height="8" rx="4" fill="#c7d2fe" />
        <path
          d="M222 186L230 206L250 214L230 222L222 242L214 222L194 214L214 206Z"
          fill="#fbbf24"
          stroke="#6366f1"
          strokeWidth="5"
          strokeLinejoin="round"
        />
      </svg>
      {showText && <span className="logo-text">Smart Notes</span>}
    </div>
  );
}
