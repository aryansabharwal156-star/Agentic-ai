# Higgsfield Creative Suite landing page

A responsive React landing page using the Higgsfield design system: charcoal surfaces, acid lime actions, cinematic imagery, and a magenta contest feature.

## Run locally

Requires Node.js 20.19+ or 22.12+ and npm.

```sh
npm ci
npm run dev -- --port 3000
```

Open http://localhost:3000. To generate the production site, run `npm run build`; output is written to `dist/`.

## Page features

- Three manually controlled hero stories
- Academy, Fable 5 / After Effects, $100K contest, Seedream, and App Builder feature previews
- Image, Video, Apps, and Learn filters
- Accessible feature dialogs with keyboard dismissal and focus return
- Responsive mobile menu and dismissible promotional offer

Calls to action continue to the public Higgsfield website. Signup, billing, model execution, and the displayed promotional offer require the actual Higgsfield service; this landing page does not process accounts or payments.

## Visual sources

Design direction: https://gitreverse.com/designs/higgsfield-ai

The Earth image is from [NASA on Unsplash](https://unsplash.com/photos/yZygONrUBe8), served locally from `public/media/space.jpg`. Academy, fashion, perfume, and landscape media are from the public Higgsfield website. Plugin, contest, app preview, and promotional graphics are built with CSS and Lucide icons. Media is bundled locally for reliable preview loading.

The existing Android project remains in `app/` and the Gradle configuration files.
