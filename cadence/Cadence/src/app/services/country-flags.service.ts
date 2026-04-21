export interface CountryFlag {
  code: string;
  name: string;
  flagSvg: string;
}

export const COUNTRY_FLAGS: { [key: string]: CountryFlag } = {
  'US': {
    code: 'US',
    name: 'United States',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#B22234" d="M0 0h640v480H0z"/>
      <path fill="#fff" d="M0 36.9h640v36.9H0zm0 73.8h640v36.9H0zm0 73.8h640v36.9H0zm0 73.8h640v36.9H0zm0 73.8h640v36.9H0zm0 73.8h640v36.9H0z"/>
      <path fill="#3C3B6E" d="M0 0h369.6v259.2H0z"/>
      <g fill="#fff">
        <g id="s18">
          <g id="s9">
            <g id="s5">
              <g id="s4">
                <path id="s" d="M3.2 2.4l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6zm0 3.6l-.8.2.2.8-.6-.6-.8.2.6-.6-.2-.8.8.2.6-.6z"/>
                <use xlink:href="#s" y="3.6"/>
                <use xlink:href="#s" y="7.2"/>
                <use xlink:href="#s" y="10.8"/>
              </g>
              <use xlink:href="#s4" x="4.8"/>
            </g>
            <use xlink:href="#s5" x="9.6"/>
          </g>
          <use xlink:href="#s9" x="19.2"/>
        </g>
        <use xlink:href="#s18" x="38.4"/>
        <use xlink:href="#s18" x="57.6"/>
        <use xlink:href="#s18" x="76.8"/>
        <use xlink:href="#s18" x="96"/>
        <use xlink:href="#s18" x="115.2"/>
        <use xlink:href="#s18" x="134.4"/>
        <use xlink:href="#s18" x="153.6"/>
        <use xlink:href="#s18" x="172.8"/>
        <use xlink:href="#s18" x="192"/>
        <use xlink:href="#s18" x="211.2"/>
        <use xlink:href="#s18" x="230.4"/>
        <use xlink:href="#s18" x="249.6"/>
        <use xlink:href="#s18" x="268.8"/>
        <use xlink:href="#s18" x="288"/>
        <use xlink:href="#s18" x="307.2"/>
        <use xlink:href="#s18" x="326.4"/>
        <use xlink:href="#s18" x="345.6"/>
        <use xlink:href="#s18" x="364.8"/>
      </g>
    </svg>`
  },
  'AU': {
    code: 'AU',
    name: 'Australia',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#012169" d="M0 0h640v480H0z"/>
      <path fill="#fff" d="M0 0l320 240v240L0 240z"/>
      <path fill="#C8102E" d="M0 0l320 240L0 480V0zm0 240h640v240H0z"/>
      <g fill="#fff">
        <path d="M464 160l-6.4 19.2 19.2-6.4-19.2-6.4zM464 320l-6.4-19.2 19.2 6.4-19.2 6.4z"/>
        <circle cx="480" cy="240" r="32"/>
      </g>
      <g fill="#C8102E">
        <path d="M464 160l6.4 19.2-19.2-6.4 19.2-6.4zM464 320l6.4-19.2-19.2 6.4 19.2 6.4z"/>
        <circle cx="480" cy="240" r="24" fill="#012169"/>
      </g>
      <g fill="#fff">
        <path d="M320 0l32 96h96l-80 64 32 96-80-64-80 64 32-96-80-64h96z"/>
      </g>
    </svg>`
  },
  'CA': {
    code: 'CA',
    name: 'Canada',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#ff0000" d="M0 0h640v480H0z"/>
      <path fill="#fff" d="M160 0h320v480H160z"/>
      <path fill="#ff0000" d="M0 0h160v480H0zm480 0h160v480H480z"/>
      <g fill="#fff">
        <path d="M320 120l-20 60h60l-20-60zm0 240l20-60h-60l20 60z"/>
      </g>
    </svg>`
  },
  'GB': {
    code: 'GB',
    name: 'United Kingdom',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#012169" d="M0 0h640v480H0z"/>
      <path fill="#fff" d="M0 0l320 240v240L0 240z"/>
      <path fill="#C8102E" d="M0 0l320 240L0 480V0zm0 240h640v240H0z"/>
      <g fill="#fff">
        <path d="M320 0l320 240-320 240V0z"/>
        <path d="M0 240h640v240H0z"/>
      </g>
      <g fill="#C8102E">
        <path d="M320 0l320 240-320 240V0z"/>
        <path d="M0 240h640v240H0z"/>
      </g>
    </svg>`
  },
  'TN': {
    code: 'TN',
    name: 'Tunisia',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#E70013" d="M0 0h640v480H0z"/>
      <circle cx="320" cy="240" r="80" fill="#fff"/>
      <circle cx="320" cy="240" r="72" fill="#E70013"/>
      <g fill="#fff">
        <path d="M320 168l16 48h48l-40 32 16 48-40-32-40 32 16-48-40-32h48z"/>
      </g>
    </svg>`
  },
  'DE': {
    code: 'DE',
    name: 'Germany',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#000" d="M0 0h640v160H0z"/>
      <path fill="#DD0000" d="M0 160h640v160H0z"/>
      <path fill="#FFCE00" d="M0 320h640v160H0z"/>
    </svg>`
  },
  'ES': {
    code: 'ES',
    name: 'Spain',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#AA151B" d="M0 0h640v480H0z"/>
      <path fill="#F1BF00" d="M0 120h640v240H0z"/>
      <g fill="#AA151B">
        <path d="M160 180l20 60h60l-40 32 20 60-40-32-40 32 20-60-40-32h60z"/>
      </g>
    </svg>`
  },
  'SA': {
    code: 'SA',
    name: 'Saudi Arabia',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#006C35" d="M0 0h640v480H0z"/>
      <g fill="#fff">
        <path d="M320 120l40 120h120l-80 64 40 120-80-64-80 64 40-120-80-64h120z"/>
        <text x="320" y="280" font-family="Arial" font-size="48" text-anchor="middle">لا إله إلا الله</text>
      </g>
    </svg>`
  },
  'FR': {
    code: 'FR',
    name: 'France',
    flagSvg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 480">
      <path fill="#002395" d="M0 0h213.3v480H0z"/>
      <path fill="#fff" d="M213.3 0h213.4v480H213.3z"/>
      <path fill="#ED2939" d="M426.7 0H640v480H426.7z"/>
    </svg>`
  }
};
