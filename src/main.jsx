import React, { useEffect, useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  ArrowUpRight,
  ArrowRight,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  Play,
  X,
  Menu,
  Image,
  Clapperboard,
  WandSparkles,
  Layers3,
  Grid2X2,
  Cpu,
  GraduationCap,
  Sparkles,
  Check,
} from "lucide-react";
import "./styles.css";

const HOME = "https://higgsfield.ai";
const features = {
  academy: {
    category: "LEARN. CREATE. REPEAT.",
    title: "Higgsfield Academy",
    description:
      "From your first prompt to your next big idea. Learn creative workflows, cinematic techniques, and the art of AI from creators who do it best.",
    image: "academy.webp",
    action: "Explore Academy",
    url: "/academy",
  },
  plugin: {
    category: "YOUR WORKFLOW. SUPERCHARGED.",
    title: "Meet your new creative plugin.",
    description:
      "Bring the power of Fable 5 into After Effects. Explore a new way to turn ideas into motion, right inside your creative workflow.",
    action: "Discover the plugin",
    url: "/plugins/after-effects",
  },
  contest: {
    category: "BIG IDEAS. BIGGER POSSIBILITIES.",
    title: "$100K App Contest",
    description:
      "Build something unexpected with Higgsfield image and video models. Explore the contest, discover what creators are making, and find your next big idea.",
    action: "Explore the contest",
    url: "/contests",
  },
  seedream: {
    category: "A NEW LEVEL OF IMAGE CREATION",
    title: "Seedream 5.0 Pro",
    description:
      "More detail. More control. More you. Turn a simple idea into striking visuals with the next generation of image creation.",
    image: "fashion.webp",
    action: "Start creating images",
    url: "/image",
  },
  builder: {
    category: "FROM WHAT IF TO WHAT’S NEXT",
    title: "Your idea. Your AI app.",
    description:
      "Build creative apps powered by Higgsfield image and video models. Bring your own perspective to a whole new world of possibilities.",
    action: "Explore App Builder",
    url: "/apps",
  },
  supercomputer: {
    category: "BUILT FOR YOUR BIGGEST IDEAS",
    title: "Higgsfield Supercomputer",
    description:
      "Connect your creative process with powerful image and video tools. Make room for bigger ideas and keep your imagination moving.",
    action: "Explore Supercomputer",
    url: "/supercomputer",
  },
  image: {
    category: "IMAGINE IT. MAKE IT.",
    title: "Image Studio",
    description:
      "Find your style, explore image models, and turn your imagination into your next extraordinary image.",
    image: "perfume.webp",
    action: "Create an image",
    url: "/image",
  },
  video: {
    category: "GIVE YOUR IDEAS MOTION",
    title: "Video Studio",
    description:
      "Discover cinematic camera moves, expressive motion, and a new way to bring your stories to life.",
    image: "space.jpg",
    action: "Create a video",
    url: "/video",
  },
};
function Brand({ small = false }) {
  return (
    <a
      className={`brand ${small ? "small" : ""}`}
      href="#"
      aria-label="Higgsfield home"
    >
      <svg viewBox="0 0 40 35" aria-hidden="true">
        <path d="M5 10C16-6 24 25 35 9M5 24C16 8 24 39 35 23" />
      </svg>
      <span>
        Higgsfield<span className="brand-dot">®</span>
      </span>
    </a>
  );
}
function Arrow({ diagonal = false, ...props }) {
  return diagonal ? (
    <ArrowUpRight size={17} {...props} />
  ) : (
    <ArrowRight size={17} {...props} />
  );
}
function FeatureModal({ id, onClose }) {
  const item = features[id];
  const closeRef = useRef(null);
  useEffect(() => {
    const previous = document.activeElement;
    closeRef.current?.focus();
    const old = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const handler = (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "Tab") {
        const items = [...document.querySelectorAll(".modal a, .modal button")];
        const first = items[0],
          last = items.at(-1);
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", handler);
    return () => {
      document.body.style.overflow = old;
      document.removeEventListener("keydown", handler);
      const candidates = [
        previous,
        document.querySelector(".menu-toggle"),
        document.querySelector(".nav-dropdown > button"),
      ];
      const target = candidates.find(
        (element) =>
          element instanceof HTMLElement &&
          element !== document.body &&
          element.isConnected &&
          element.getClientRects().length > 0 &&
          !element.disabled,
      );
      target?.focus();
    };
  }, [onClose]);
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          className="modal-close icon-button"
          ref={closeRef}
          onClick={onClose}
          aria-label="Close preview"
        >
          <X />
        </button>
        {item.image ? (
          <img className="modal-image" src={`/media/${item.image}`} alt="" />
        ) : (
          <div className={`modal-art ${id}`}>
            <Sparkles size={68} />
            <span>
              {id === "contest"
                ? "$100K"
                : id === "plugin"
                  ? "Ae + Fable 5"
                  : "Make something extraordinary."}
            </span>
          </div>
        )}
        <div className="modal-copy">
          <span className="eyebrow">{item.category}</span>
          <h2 id="modal-title">{item.title}</h2>
          <p>{item.description}</p>
          <a
            className="button lime"
            href={HOME + item.url}
            target="_blank"
            rel="noreferrer"
          >
            {item.action}
            <Arrow diagonal />
          </a>
          <span className="external-note">
            Continue on Higgsfield <ArrowUpRight size={12} />
          </span>
        </div>
      </section>
    </div>
  );
}
function App() {
  const [modal, setModal] = useState(null),
    [mobileMenu, setMobileMenu] = useState(false),
    [dropdown, setDropdown] = useState(null),
    [filter, setFilter] = useState("All tools"),
    [slide, setSlide] = useState(0);
  const [offerDismissed, setOfferDismissed] = useState(false);
  const slides = [
    {
      label: "THE NEXT GENERATION OF CREATION",
      first: "Your imagination.",
      second: "No limits.",
      text: "One creative universe. Every tool you need to make the impossible.",
      image: "space.jpg",
      title: "A new perspective",
      type: "MADE WITH HIGGSFIELD",
    },
    {
      label: "FROM THE FIRST FRAME TO THE FINAL CUT",
      first: "Big ideas.",
      second: "In motion.",
      text: "Turn a spark of inspiration into your next cinematic story.",
      image: "landscape.webp",
      title: "Beyond the ordinary",
      type: "EXPLORE VIDEO CREATION",
    },
    {
      label: "YOUR STYLE. WITHOUT COMPROMISE.",
      first: "Dream it.",
      second: "Make it real.",
      text: "Create extraordinary images with a world of possibilities at your fingertips.",
      image: "fashion.webp",
      title: "A world of your own",
      type: "EXPLORE IMAGE CREATION",
    },
  ];
  const hero = slides[slide];
  useEffect(() => {
    const close = (e) => {
      if (e.key === "Escape") {
        setDropdown(null);
        setMobileMenu(false);
      }
    };
    document.addEventListener("keydown", close);
    return () => document.removeEventListener("keydown", close);
  }, []);
  const openFeature = (id) => {
    setModal(id);
    setDropdown(null);
    setMobileMenu(false);
  };
  const products = [
    {
      id: "academy",
      name: "Higgsfield Academy",
      text: "Your next creative breakthrough starts here.",
      type: "Learn",
      icon: GraduationCap,
      image: "academy.webp",
      tag: "FREE TO LEARN",
    },
    {
      id: "supercomputer",
      name: "Supercomputer",
      text: "Big ideas. Serious creative power.",
      type: "Apps",
      icon: Cpu,
      art: "compute",
      tag: "BUILT FOR CREATORS",
    },
    {
      id: "image",
      name: "Image Studio",
      text: "Turn a little imagination into a lot of wow.",
      type: "Image",
      icon: Image,
      image: "perfume.webp",
      tag: "IMAGE GENERATION",
    },
    {
      id: "video",
      name: "Video Studio",
      text: "Every great story deserves to move.",
      type: "Video",
      icon: Clapperboard,
      image: "landscape.webp",
      tag: "VIDEO GENERATION",
    },
  ];
  return (
    <>
      <header className="header">
        <div className="header-main">
          <Brand />
          <nav
            className={`main-nav ${mobileMenu ? "is-open" : ""}`}
            aria-label="Main navigation"
          >
            <a
              className="active"
              href="#explore"
              onClick={() => setMobileMenu(false)}
            >
              Explore
            </a>
            <div className="nav-dropdown">
              <button
                aria-expanded={dropdown === "create"}
                onClick={() =>
                  setDropdown(dropdown === "create" ? null : "create")
                }
              >
                Create <ChevronDown size={13} />
              </button>
              {dropdown === "create" && (
                <div className="dropdown-panel">
                  <button onClick={() => openFeature("image")}>
                    <Image size={17} />
                    <span>
                      Create an image
                      <small>Make your imagination visible</small>
                    </span>
                  </button>
                  <button onClick={() => openFeature("video")}>
                    <Clapperboard size={17} />
                    <span>
                      Create a video<small>Bring your story to life</small>
                    </span>
                  </button>
                  <button onClick={() => openFeature("builder")}>
                    <Grid2X2 size={17} />
                    <span>
                      Build an app<small>Make something of your own</small>
                    </span>
                  </button>
                </div>
              )}
            </div>
            <a href="#community" onClick={() => setMobileMenu(false)}>
              Community
            </a>
            <a
              href={HOME + "/pricing"}
              target="_blank"
              rel="noreferrer"
              onClick={() => setMobileMenu(false)}
            >
              Pricing
            </a>
            <button onClick={() => openFeature("academy")}>
              Academy <span className="tiny-tag">NEW</span>
            </button>
          </nav>
          <div className="header-actions">
            <a className="login" href={HOME} target="_blank" rel="noreferrer">
              Log in
            </a>
            <a
              className="button lime header-cta"
              href={HOME}
              target="_blank"
              rel="noreferrer"
            >
              Start creating <Arrow diagonal />
            </a>
            <button
              className="menu-toggle icon-button"
              aria-label={mobileMenu ? "Close menu" : "Open menu"}
              aria-expanded={mobileMenu}
              onClick={() => setMobileMenu(!mobileMenu)}
            >
              {mobileMenu ? <X /> : <Menu />}
            </button>
          </div>
        </div>
        <nav className="tools-nav" aria-label="Creative tools">
          <button
            className="selected"
            onClick={() => {
              setFilter("All tools");
              document
                .getElementById("explore")
                .scrollIntoView({ behavior: "auto" });
            }}
          >
            <Grid2X2 />
            All tools
          </button>
          <button onClick={() => openFeature("image")}>
            <Image />
            Image
          </button>
          <button onClick={() => openFeature("video")}>
            <Clapperboard />
            Video
          </button>
          <button onClick={() => openFeature("image")}>
            <WandSparkles />
            Edit
          </button>
          <button onClick={() => openFeature("video")}>
            <Layers3 />
            Visual effects
          </button>
          <button onClick={() => openFeature("builder")}>
            <Cpu />
            Apps
            <span className="new-dot" />
          </button>
          <span className="tools-spacer" />
          <span className="powered">
            <span />
            Built for your next big idea.
          </span>
        </nav>
      </header>
      <main>
        <section
          className={`hero hero-${slide}`}
          aria-label="Featured creative tools"
        >
          <img
            className="hero-image"
            src={`/media/${hero.image}`}
            alt={
              slide === 0
                ? "The glowing curve of Earth seen from orbit"
                : slide === 1
                  ? "An open road through a cinematic mountain valley"
                  : "A bold streetwear fashion portrait"
            }
            fetchPriority="high"
          />
          <div className="hero-shade" />
          <div className="hero-copy">
            <span className="eyebrow">
              <span className="lime-dot" />
              {hero.label}
            </span>
            <h1>
              {hero.first}
              <br />
              <span>{hero.second}</span>
            </h1>
            <p>{hero.text}</p>
            <div className="hero-buttons">
              <a
                className="button lime"
                href={HOME}
                target="_blank"
                rel="noreferrer"
              >
                Start creating for free <Arrow diagonal />
              </a>
              <button
                className="reel-button"
                onClick={() => openFeature(slide === 2 ? "image" : "video")}
              >
                <span className="play-circle">
                  <Play size={12} fill="currentColor" />
                </span>
                Explore what’s possible
              </button>
            </div>
            <div className="hero-footnote">
              <span>
                <Check size={12} /> No creative limits
              </span>
              <i />
              <span>Just your imagination.</span>
            </div>
          </div>
          <div className="hero-caption">
            <span className="crosshair">+</span>
            <div>
              <span>{hero.type}</span>
              <strong>{hero.title}</strong>
            </div>
          </div>
          <div className="hero-bottom">
            <div className="hero-pages">
              {slides.map((s, i) => (
                <button
                  key={s.title}
                  onClick={() => setSlide(i)}
                  className={i === slide ? "current" : ""}
                  aria-label={`Show featured story ${i + 1}`}
                  aria-pressed={i === slide}
                >
                  <span>0{i + 1}</span>
                  <i />
                </button>
              ))}
            </div>
            <div className="hero-controls">
              <button
                className="icon-button"
                onClick={() => setSlide((slide + 2) % 3)}
                aria-label="Previous featured story"
              >
                <ChevronLeft size={17} />
              </button>
              <button
                className="icon-button"
                onClick={() => setSlide((slide + 1) % 3)}
                aria-label="Next featured story"
              >
                <ChevronRight size={17} />
              </button>
            </div>
          </div>
        </section>
        <section className="featured section" id="featured">
          <div className="section-heading">
            <div>
              <span className="eyebrow muted">THE CREATIVE FRONTIER</span>
              <h2>New. Next. Yours.</h2>
            </div>
            <span className="section-subtitle">
              Fresh tools. Bigger possibilities.
            </span>
          </div>
          <div className="feature-grid">
            <button
              className="feature-card"
              onClick={() => openFeature("academy")}
            >
              <div className="card-media academy">
                <img
                  src="/media/academy.webp"
                  alt="Higgsfield Academy host on a cinematic studio set"
                />
                <span className="card-badge">HIGGSFIELD ORIGINALS</span>
                <span className="media-play">
                  <Play size={14} fill="currentColor" />
                </span>
                <div className="media-caption">
                  A little learning.
                  <br />A lot of possibility.
                </div>
              </div>
              <div className="card-info">
                <div>
                  <span className="card-kicker">LEVEL UP YOUR CREATIVITY</span>
                  <h3>Higgsfield Academy</h3>
                  <p>Your next great idea starts with a little know-how.</p>
                </div>
                <span className="card-arrow">
                  <Arrow diagonal />
                </span>
              </div>
            </button>
            <button
              className="feature-card"
              onClick={() => openFeature("plugin")}
            >
              <div className="card-media plugin">
                <span className="card-badge">NEW RELEASE</span>
                <div className="plugin-orbit orbit-one" />
                <div className="plugin-orbit orbit-two" />
                <div className="ae-logo">Ae</div>
                <span className="plugin-spark">✳</span>
                <span className="fable-label">
                  FABLE <b>5</b>
                </span>
                <span className="plugin-caption">
                  Less friction. More creation.
                </span>
              </div>
              <div className="card-info">
                <div>
                  <span className="card-kicker">MADE FOR YOUR WORKFLOW</span>
                  <h3>After Effects. Meet AI.</h3>
                  <p>Your favorite tools, now powered by Fable 5.</p>
                </div>
                <span className="card-arrow">
                  <Arrow diagonal />
                </span>
              </div>
            </button>
            <button
              className="feature-card"
              onClick={() => openFeature("contest")}
            >
              <div className="card-media contest">
                <span className="card-badge pink-badge">
                  <span />
                  CREATORS WANTED
                </span>
                <span className="contest-dots" />
                <div className="contest-prize">
                  <span>BUILD SOMETHING BIG.</span>
                  <strong>
                    $100<span>K</span>
                  </strong>
                  <span>APP BUILDER CONTEST</span>
                </div>
                <span className="contest-star">✳</span>
                <span className="contest-bottom">
                  YOUR IDEA COULD BE THE NEXT BIG THING.
                  <ArrowUpRight size={18} />
                </span>
              </div>
              <div className="card-info">
                <div>
                  <span className="card-kicker">
                    BIG IDEAS DESERVE BIG REWARDS
                  </span>
                  <h3>The $100K App Contest</h3>
                  <p>Build it. Share it. Make your mark.</p>
                </div>
                <span className="card-arrow">
                  <Arrow diagonal />
                </span>
              </div>
            </button>
          </div>
          <div className="wide-grid">
            <button
              className="wide-card seedream"
              onClick={() => openFeature("seedream")}
            >
              <div className="wide-card-copy">
                <span className="card-badge">
                  JUST DROPPED <span className="new-dot" />
                </span>
                <h3>
                  Every detail.
                  <br />
                  Exactly as imagined.
                </h3>
                <p>Meet Seedream 5.0 Pro.</p>
                <span className="text-cta">
                  Create something unreal <Arrow diagonal />
                </span>
              </div>
              <div className="seedream-images">
                <img
                  src="/media/fashion.webp"
                  alt="Expressive editorial streetwear photography"
                />
                <img
                  src="/media/perfume.webp"
                  alt="Detailed creative perfume product photography"
                />
                <span className="image-label">
                  <Sparkles size={12} /> Seedream 5.0 Pro
                </span>
              </div>
            </button>
            <button
              className="wide-card app-builder"
              onClick={() => openFeature("builder")}
            >
              <div className="wide-card-copy">
                <span className="card-badge">INTRODUCING APP BUILDER</span>
                <h3>
                  What if your idea
                  <br />
                  was an app?
                </h3>
                <p>Our models. Your next big thing.</p>
                <span className="text-cta">
                  Build with Higgsfield <Arrow diagonal />
                </span>
              </div>
              <div className="builder-art">
                <div className="app-window">
                  <div className="window-bar">
                    <i />
                    <i />
                    <i />
                    <span>your-next-big-idea.app</span>
                  </div>
                  <div className="mini-app">
                    <Sparkles size={16} />
                    <strong>Make a little magic.</strong>
                    <div className="mini-pictures">
                      <img src="/media/space.jpg" alt="" />
                      <img src="/media/perfume.webp" alt="" />
                    </div>
                    <div className="mini-input">
                      A world that doesn’t exist yet <ArrowRight size={10} />
                    </div>
                    <span className="mini-generate">
                      Generate something amazing <Sparkles size={9} />
                    </span>
                  </div>
                </div>
                <span className="floating-model">
                  <span className="lime-dot" />
                  Powered by Higgsfield
                </span>
              </div>
            </button>
          </div>
        </section>
        {!offerDismissed && (
          <section className="offer" id="offer">
            <div className="offer-symbol">
              <span>✳</span>
              <i />
              <i />
              <i />
            </div>
            <div className="offer-copy">
              <span className="eyebrow">
                A LITTLE EXTRA FOR YOUR NEXT BIG IDEA
              </span>
              <h2>
                More imagination.
                <br />
                <span>20% less.</span>
              </h2>
              <p>Sign up and unlock an extra 20% off your first plan.</p>
            </div>
            <div className="offer-action">
              <a
                className="button lime"
                href={HOME}
                target="_blank"
                rel="noreferrer"
              >
                Unlock my extra 20% <Arrow diagonal />
              </a>
              <span>New creators. Endless possibilities.</span>
            </div>
            <button
              className="offer-close icon-button"
              aria-label="Dismiss offer"
              onClick={() => setOfferDismissed(true)}
            >
              <X size={17} />
            </button>
          </section>
        )}
        <section className="explore section" id="explore">
          <div className="section-heading">
            <div>
              <span className="eyebrow muted">FIND YOUR NEXT OBSESSION</span>
              <h2>A universe of possibilities.</h2>
            </div>
            <a
              href="#explore"
              className="view-all"
              onClick={() => setFilter("All tools")}
            >
              Explore all tools <Arrow diagonal />
            </a>
          </div>
          <div className="filter-row" aria-label="Filter creative tools">
            {["All tools", "Image", "Video", "Apps", "Learn"].map((f) => (
              <button
                key={f}
                className={filter === f ? "active" : ""}
                onClick={() => setFilter(f)}
                aria-pressed={filter === f}
              >
                {f === "All tools" && <Grid2X2 size={13} />} {f}
              </button>
            ))}
            <span>
              {filter === "All tools"
                ? "Something for every kind of creator."
                : `Explore ${filter.toLowerCase()} tools and possibilities.`}
            </span>
          </div>
          <div className="explore-grid">
            {products
              .filter((p) => filter === "All tools" || p.type === filter)
              .map((p) => (
                <button
                  key={p.id}
                  className="explore-card"
                  onClick={() => openFeature(p.id)}
                >
                  <div className={`explore-media ${p.art || ""}`}>
                    {p.image ? (
                      <img src={`/media/${p.image}`} alt="" loading="lazy" />
                    ) : (
                      <>
                        <div className="compute-grid" />
                        <Cpu className="compute-chip" size={64} />
                        <span className="compute-title">Supercomputer</span>
                        <span className="compute-sub">
                          THE POWER TO THINK BIGGER.
                        </span>
                      </>
                    )}
                    <span className="explore-tag">{p.tag}</span>
                    <span className="explore-icon">
                      <p.icon size={16} />
                    </span>
                  </div>
                  <div className="explore-info">
                    <h3>
                      {p.name}
                      <Arrow diagonal />
                    </h3>
                    <p>{p.text}</p>
                  </div>
                </button>
              ))}
          </div>
        </section>
        <section className="community-banner" id="community">
          <div className="community-mark">✳</div>
          <div>
            <span className="eyebrow muted">MADE OF IDEAS. MADE BY YOU.</span>
            <h2>The next wave of creativity is yours.</h2>
            <p>Join a world of creators making things the world hasn’t seen.</p>
          </div>
          <a
            className="button outline"
            href="https://higgsfield.ai"
            target="_blank"
            rel="noreferrer"
          >
            Meet the community <Arrow diagonal />
          </a>
        </section>
      </main>
      <footer>
        <Brand small />
        <span>A new dimension of creativity.</span>
        <div>
          <a href={HOME + "/terms-of-use"} target="_blank" rel="noreferrer">
            Terms
          </a>
          <a href={HOME + "/privacy-policy"} target="_blank" rel="noreferrer">
            Privacy
          </a>
          <span>© {new Date().getFullYear()} Higgsfield AI</span>
        </div>
        <a className="back-top" href="#" aria-label="Back to top">
          ↑
        </a>
      </footer>
      {modal && <FeatureModal id={modal} onClose={() => setModal(null)} />}
    </>
  );
}
createRoot(document.getElementById("root")).render(<App />);
