// Steam exposes public store artwork on its CDN, addressable by app id — no API key needed.
const STEAM_CDN = 'https://cdn.cloudflare.steamstatic.com/steam/apps'

/** Wide header artwork (460×215), ideal for cards and hero banners. */
export function steamHeaderImage(appId: number): string {
  return `${STEAM_CDN}/${appId}/header.jpg`
}

/** Small horizontal capsule (231×87), lighter for dense lists. */
export function steamCapsuleImage(appId: number): string {
  return `${STEAM_CDN}/${appId}/capsule_231x87.jpg`
}
