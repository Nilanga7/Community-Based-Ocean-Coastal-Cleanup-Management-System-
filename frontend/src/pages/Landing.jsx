import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Icon } from '../design-system';
import { api } from '../api/index.js';
import { mediaUrl } from '../api/client.js';
import { useApi } from '../hooks/useApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { SEVERITY_LABEL, formatDate, locationLine } from '../lib/format.js';
import './landing.css';

const SLIDE_MS = 6000;

const SLIDES = [
  {
    eyebrow: 'Report',
    title: 'Spot pollution on the coast? Report it in a minute.',
    body: 'Snap a photo, drop your location and describe what you see. People nearby are alerted to help confirm it.',
    cta: 'Report a polluted site',
    icon: 'camera',
    action: 'submit',
  },
  {
    eyebrow: 'Verify',
    title: 'The community checks every report before it moves on.',
    body: 'Five confirmations and 75% trust send a report to administrators, then to the government officer.',
    cta: 'Browse reports',
    icon: 'badge-check',
    action: 'feed',
  },
  {
    eyebrow: 'Clean up',
    title: 'Approved sites become cleanup projects anyone can join.',
    body: 'Administrators assign volunteers, divers and equipment. Follow progress until the site is clean.',
    cta: 'See cleanup projects',
    icon: 'hand-heart',
    action: 'projects',
  },
];

const TILES = [
  { label: 'Report', hint: 'Photo and location', icon: 'camera', color: 'var(--buoy-400)', bg: 'rgba(245, 196, 91, 0.16)', to: '/app/submit', auth: true },
  { label: 'Verify', hint: 'Confirm what you see', icon: 'badge-check', color: 'var(--tide-300)', bg: 'rgba(53, 194, 183, 0.16)', to: '/app' },
  { label: 'Clean up', hint: 'Join a project', icon: 'hand-heart', color: 'var(--sea-300)', bg: 'rgba(127, 198, 221, 0.16)', to: '/app/cleanups' },
  { label: 'Dive', hint: 'Diver opportunities', icon: 'anchor', color: '#ff9f8a', bg: 'rgba(255, 159, 138, 0.16)', to: '/app/opportunities', auth: true },
];

const STEPS = [
  { icon: 'camera', title: 'Someone reports it', body: 'A citizen or diver submits photos, severity and the exact location.' },
  { icon: 'users', title: 'The community verifies', body: 'Neighbours confirm or dispute. Five confirmations and 75% trust pass it on.' },
  { icon: 'shield-check', title: 'Officials approve', body: 'An administrator reviews it, then the government officer approves it.' },
  { icon: 'hand-heart', title: 'It becomes a project', body: 'Resources are assigned, volunteers join and progress is tracked to the end.' },
];

/** Adds `is-visible` to `.tl-reveal` elements as they scroll into view. */
function useReveal(deps) {
  const ref = React.useRef(null);
  React.useEffect(() => {
    const root = ref.current;
    if (!root) return undefined;
    const items = root.querySelectorAll('.tl-reveal:not(.is-visible)');
    if (!('IntersectionObserver' in window)) {
      items.forEach((el) => el.classList.add('is-visible'));
      return undefined;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.15, rootMargin: '0px 0px -40px 0px' },
    );
    items.forEach((el) => observer.observe(el));
    return () => observer.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  return ref;
}

/** Counts up to `value` the first time it scrolls into view. */
function CountUp({ value }) {
  const ref = React.useRef(null);
  const [shown, setShown] = React.useState(0);

  React.useEffect(() => {
    const node = ref.current;
    if (!node || value == null) return undefined;
    const reduce = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    let frame;
    const run = () => {
      if (reduce) {
        setShown(value);
        return;
      }
      const start = performance.now();
      const tick = (now) => {
        const t = Math.min(1, (now - start) / 1200);
        setShown(Math.round(value * (1 - (1 - t) ** 3)));
        if (t < 1) frame = requestAnimationFrame(tick);
      };
      frame = requestAnimationFrame(tick);
    };
    if (!('IntersectionObserver' in window)) {
      run();
      return () => cancelAnimationFrame(frame);
    }
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        run();
        observer.disconnect();
      }
    });
    observer.observe(node);
    return () => {
      observer.disconnect();
      cancelAnimationFrame(frame);
    };
  }, [value]);

  return <span ref={ref}>{value == null ? '—' : shown}</span>;
}

function Waves() {
  const wave = (fill, d) => (
    <svg viewBox="0 0 2880 180" preserveAspectRatio="none" aria-hidden>
      <path fill={fill} d={d} />
    </svg>
  );
  const path = 'M0 90 C 240 30 480 150 720 90 C 960 30 1200 150 1440 90 C 1680 30 1920 150 2160 90 C 2400 30 2640 150 2880 90 L2880 180 L0 180 Z';
  const path2 = 'M0 110 C 300 60 420 160 720 110 C 1020 60 1140 160 1440 110 C 1740 60 1860 160 2160 110 C 2460 60 2580 160 2880 110 L2880 180 L0 180 Z';
  const path3 = 'M0 130 C 360 100 360 170 720 130 C 1080 100 1080 170 1440 130 C 1800 100 1800 170 2160 130 C 2520 100 2520 170 2880 130 L2880 180 L0 180 Z';
  return (
    <div className="tl-waves">
      <div className="tl-wave-1" style={{ position: 'absolute', inset: 0 }}>{wave('#2483a8', path)}</div>
      <div className="tl-wave-2" style={{ position: 'absolute', inset: 0 }}>{wave('#068b85', path2)}</div>
      <div className="tl-wave-3" style={{ position: 'absolute', inset: 0 }}>{wave('#05171f', path3)}</div>
    </div>
  );
}

/** A small phone showing the latest real report, like the app itself. */
function PhoneMock({ report }) {
  return (
    <div className="tl-phone" style={{ width: 270, margin: '0 auto', padding: 10, borderRadius: 42, background: '#0b1216', boxShadow: '0 40px 80px rgba(0,0,0,0.55), inset 0 0 0 2px rgba(255,255,255,0.08)' }}>
      <div style={{ borderRadius: 34, overflow: 'hidden', background: 'var(--surface-page)', color: 'var(--text-strong)' }}>
        <div style={{ height: 26, display: 'flex', justifyContent: 'center', alignItems: 'center', background: 'var(--sea-900)' }}>
          <span style={{ width: 70, height: 16, borderRadius: 10, background: '#0b1216' }} />
        </div>
        <div style={{ padding: '10px 14px', background: 'var(--sea-900)', display: 'flex', alignItems: 'center', gap: 6, color: 'var(--white)' }}>
          <Icon name="waves-horizontal" size="sm" color="var(--tide-300)" />
          <span style={{ font: '700 13px/1 var(--font-display)' }}>Tideline</span>
        </div>
        <div style={{ padding: 12, display: 'flex', flexDirection: 'column', gap: 10 }}>
          <span style={{ font: '700 16px/1.2 var(--font-display)' }}>Nearby reports</span>
          <div style={{ borderRadius: 14, overflow: 'hidden', border: '1px solid var(--border-subtle)', background: 'var(--surface-card)' }}>
            <div style={{ height: 110, background: 'linear-gradient(160deg, var(--sea-300), var(--tide-500))', position: 'relative' }}>
              {report?.evidence?.find((e) => e.contentType?.startsWith('image/')) ? (
                <img
                  alt=""
                  src={mediaUrl(report.evidence.find((e) => e.contentType?.startsWith('image/')).url)}
                  style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                />
              ) : (
                <CoastScene compact />
              )}
            </div>
            <div style={{ padding: 10, display: 'flex', flexDirection: 'column', gap: 4 }}>
              <span style={{ font: '600 10px/1 var(--font-mono)', color: 'var(--text-muted)' }}>{report?.reference || 'SR-2466'}</span>
              <span style={{ font: '600 12px/1.3 var(--font-body)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {report?.title || 'Plastic debris along the beach'}
              </span>
              <div style={{ height: 5, borderRadius: 4, background: 'var(--gray-200, #e5e7eb)', overflow: 'hidden', marginTop: 4 }}>
                <div style={{ width: `${Math.max(12, report?.trustPercentage ?? 64)}%`, height: '100%', background: 'var(--tide-500)' }} />
              </div>
              <span style={{ font: '500 10px/1 var(--font-body)', color: 'var(--text-muted)' }}>{report?.trustPercentage ?? 64}% trust</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: 6 }}>
            <span style={{ flex: 1, textAlign: 'center', padding: '8px 0', borderRadius: 10, background: 'var(--accent)', color: 'var(--white)', font: '600 11px/1 var(--font-body)' }}>Confirm</span>
            <span style={{ flex: 1, textAlign: 'center', padding: '8px 0', borderRadius: 10, border: '1px solid var(--border-default)', font: '600 11px/1 var(--font-body)' }}>Dispute</span>
          </div>
        </div>
      </div>
    </div>
  );
}

/** Beach, sea and a little litter, drawn so the page needs no stock photos. */
function CoastScene({ compact = false }) {
  return (
    <svg viewBox="0 0 400 260" preserveAspectRatio="xMidYMid slice" style={{ width: '100%', height: '100%', display: 'block' }} aria-hidden>
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#7fc6dd" />
          <stop offset="1" stopColor="#dcf0f6" />
        </linearGradient>
        <linearGradient id="sea" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#2483a8" />
          <stop offset="1" stopColor="#068b85" />
        </linearGradient>
      </defs>
      <rect width="400" height="260" fill="url(#sky)" />
      <circle cx="320" cy="62" r="30" fill="#f5c45b" />
      <path d="M0 120 C 80 105 140 135 220 118 C 300 102 350 128 400 115 L400 190 L0 190 Z" fill="url(#sea)" />
      <path d="M0 150 C 90 138 160 162 250 148 C 320 138 360 155 400 147" stroke="#b4e0ec" strokeWidth="3" fill="none" opacity="0.7" />
      <path d="M0 180 C 90 168 200 196 300 178 C 350 170 380 176 400 172 L400 260 L0 260 Z" fill="#f3e2bb" />
      {compact ? null : (
        <>
          <path d="M40 260 L40 170 M40 170 C 20 160 10 150 0 152 M40 170 C 60 150 80 148 96 152 M40 170 C 30 140 36 128 44 118 M40 170 C 58 160 76 170 84 180" stroke="#0f3446" strokeWidth="4" fill="none" strokeLinecap="round" />
        </>
      )}
      <rect x="150" y="200" width="26" height="10" rx="4" fill="#47a6c7" transform="rotate(-18 163 205)" />
      <rect x="230" y="214" width="20" height="9" rx="4" fill="#e09414" transform="rotate(22 240 218)" />
      <circle cx="290" cy="205" r="6" fill="#ffffff" stroke="#b4e0ec" strokeWidth="2" />
      <path d="M110 222 l14 -4 l6 8 l-12 5 z" fill="#ff9f8a" />
    </svg>
  );
}

/** Reef with a diver's bubbles, for the impact panel. */
function ReefScene() {
  return (
    <svg viewBox="0 0 400 300" preserveAspectRatio="xMidYMid slice" style={{ width: '100%', height: '100%', display: 'block' }} aria-hidden>
      <defs>
        <linearGradient id="deep" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#1a6180" />
          <stop offset="1" stopColor="#05171f" />
        </linearGradient>
      </defs>
      <rect width="400" height="300" fill="url(#deep)" />
      <path d="M0 40 L400 20" stroke="#7fc6dd" strokeWidth="1" opacity="0.25" />
      <path d="M60 0 L150 300 M180 0 L230 300 M300 0 L280 300" stroke="#b4e0ec" strokeWidth="18" opacity="0.05" />
      <g fill="#35c2b7" opacity="0.9">
        <path d="M40 300 C 40 250 30 230 50 210 C 60 240 70 250 66 300 Z" />
        <path d="M80 300 C 76 240 96 220 92 190 C 110 220 110 260 104 300 Z" />
      </g>
      <g fill="#ff9f8a">
        <circle cx="300" cy="262" r="26" />
        <circle cx="330" cy="250" r="18" />
        <circle cx="275" cy="248" r="14" />
      </g>
      <path d="M340 300 C 340 270 360 250 350 220 M360 300 C 370 270 390 260 380 230" stroke="#f5c45b" strokeWidth="6" fill="none" strokeLinecap="round" />
      <g transform="translate(170 120) rotate(-12)">
        <ellipse cx="0" cy="0" rx="46" ry="12" fill="#0a2532" />
        <circle cx="52" cy="-4" r="11" fill="#0a2532" />
        <rect x="40" y="-10" width="14" height="8" rx="3" fill="#7bd9d0" />
        <rect x="-30" y="-20" width="34" height="10" rx="5" fill="#e09414" />
        <path d="M-46 0 L-78 -12 L-74 10 Z" fill="#035e5a" />
      </g>
      <g fill="none" stroke="#dcf0f6" strokeWidth="2" opacity="0.8">
        <circle cx="236" cy="96" r="5" />
        <circle cx="244" cy="74" r="7" />
        <circle cx="236" cy="48" r="4" />
        <circle cx="250" cy="28" r="6" />
      </g>
      <rect x="120" y="240" width="40" height="16" rx="6" fill="#dcf0f6" opacity="0.8" transform="rotate(14 140 248)" />
    </svg>
  );
}

function SectionTitle({ eyebrow, title, action }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', gap: 16, marginBottom: 'var(--space-6)', flexWrap: 'wrap' }}>
      <div>
        {eyebrow ? <span style={{ font: '600 13px/1 var(--font-body)', letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--tide-300)' }}>{eyebrow}</span> : null}
        <h2 style={{ font: '700 34px/1.12 var(--font-display)', letterSpacing: '-0.02em', color: 'var(--white)', marginTop: eyebrow ? 8 : 0 }}>{title}</h2>
      </div>
      {action}
    </div>
  );
}

export default function Landing() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const summary = useApi(() => api.analytics.summary(), []);
  const reports = useApi(() => api.reports.list({ size: 3 }), []);
  const projects = useApi(() => api.projects.list({}), []);

  const [slide, setSlide] = React.useState(0);
  const [paused, setPaused] = React.useState(false);
  const [scrolled, setScrolled] = React.useState(false);
  const [latest, setLatest] = React.useState('reports');

  const latestReports = reports.data?.content || [];
  const latestProjects = (projects.data || []).filter((p) => p.status !== 'COMPLETED').slice(0, 3);
  const pageRef = useReveal([latestReports.length, latestProjects.length, latest]);

  React.useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  React.useEffect(() => {
    if (paused) return undefined;
    const timer = setTimeout(() => setSlide((s) => (s + 1) % SLIDES.length), SLIDE_MS);
    return () => clearTimeout(timer);
  }, [slide, paused]);

  const goTo = (to, needsAuth) => navigate(needsAuth && !user ? '/login' : to, needsAuth && !user ? { state: { from: to } } : undefined);
  const home = user?.role === 'ADMIN' || user?.role === 'AUTHORITY' ? '/console' : '/app';

  function runSlide(action) {
    if (action === 'submit') goTo('/app/submit', true);
    else if (action === 'projects') navigate('/app/cleanups');
    else navigate('/app');
  }

  const current = SLIDES[slide];
  const s = summary.data;

  return (
    <div ref={pageRef} className="tl-landing" style={{ minHeight: '100%' }}>
      <header className={`tl-nav${scrolled ? ' is-scrolled' : ''}`}>
        <div className="tl-wrap" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none' }}>
            <Icon name="waves-horizontal" size="lg" color="var(--tide-300)" />
            <span style={{ font: '700 22px/1 var(--font-display)', letterSpacing: '-0.03em', color: 'var(--white)' }}>Tideline</span>
          </Link>
          <nav className="tl-nav-links" aria-label="Main" style={{ display: 'flex', gap: 'var(--space-6)' }}>
            <a href="#how-it-works">How it works</a>
            <a href="#impact">Impact</a>
            <a href="#latest">Latest</a>
            <Link to="/app/cleanups">Projects</Link>
          </nav>
          {user ? (
            <Button variant="inverse" iconRight="arrow-right" onClick={() => navigate(home)}>
              Continue as {user.fullName.split(' ')[0]}
            </Button>
          ) : (
            <div style={{ display: 'flex', gap: 8 }}>
              <Button variant="inverse" onClick={() => navigate('/login')}>Sign in</Button>
              <Button onClick={() => navigate('/register')}>Join Tideline</Button>
            </div>
          )}
        </div>
      </header>

      <section
        className={`tl-hero${paused ? ' is-paused' : ''}`}
        onMouseEnter={() => setPaused(true)}
        onMouseLeave={() => setPaused(false)}
        onFocus={() => setPaused(true)}
        onBlur={() => setPaused(false)}
        aria-roledescription="carousel"
        aria-label="How Tideline works"
        style={{ '--tl-slide-ms': `${SLIDE_MS}ms` }}
      >
        <div className="tl-wrap tl-hero-grid">
          <div>
            <div key={slide} className="tl-slide" aria-live="polite">
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, padding: '6px 12px', borderRadius: 999, background: 'rgba(53, 194, 183, 0.16)', border: '1px solid rgba(53, 194, 183, 0.35)', color: 'var(--tide-200)', font: 'var(--text-label)' }}>
                <Icon name={current.icon} size="xs" />
                {current.eyebrow} · Step {slide + 1} of {SLIDES.length}
              </span>
              <h1 className="tl-hero-title" style={{ color: 'var(--white)', marginTop: 'var(--space-5)', maxWidth: 640 }}>
                {current.title}
              </h1>
              <p style={{ font: 'var(--text-body-lg)', color: 'rgba(255, 255, 255, 0.72)', marginTop: 'var(--space-4)', maxWidth: 560 }}>
                {current.body}
              </p>
              <div style={{ display: 'flex', gap: 'var(--space-3)', marginTop: 'var(--space-6)', flexWrap: 'wrap' }}>
                <Button size="lg" iconLeft={current.icon} onClick={() => runSlide(current.action)}>
                  {current.cta}
                </Button>
                <Button size="lg" variant="inverse" iconRight="arrow-right" onClick={() => document.getElementById('how-it-works')?.scrollIntoView({ behavior: 'smooth' })}>
                  How it works
                </Button>
              </div>
            </div>

            <div className="tl-dots" role="tablist" aria-label="Choose a slide" style={{ display: 'flex', gap: 8, marginTop: 'var(--space-8)' }}>
              {SLIDES.map((item, index) => (
                <button
                  key={item.eyebrow}
                  type="button"
                  role="tab"
                  aria-label={`${item.eyebrow}: slide ${index + 1}`}
                  aria-current={index === slide}
                  aria-selected={index === slide}
                  onClick={() => setSlide(index)}
                />
              ))}
            </div>
          </div>

          <div className="tl-phone-col">
            <PhoneMock report={latestReports[0]} />
          </div>
        </div>
        <Waves />
      </section>

      <div className="tl-wrap">
        <div className="tl-tiles">
          {TILES.map((tile, index) => (
            <button
              key={tile.label}
              type="button"
              className="tl-tile tl-reveal"
              style={{ '--tl-delay': `${index * 80}ms` }}
              onClick={() => goTo(tile.to, tile.auth)}
            >
              <span>
                <span style={{ display: 'block', font: '700 18px/1.2 var(--font-display)' }}>{tile.label}</span>
                <span style={{ display: 'flex', alignItems: 'center', gap: 4, font: 'var(--text-caption)', color: 'rgba(255, 255, 255, 0.6)', marginTop: 4 }}>
                  {tile.hint}
                  <Icon name="arrow-right" size="xs" className="tl-arrow" />
                </span>
              </span>
              <span className="tl-tile-icon" style={{ background: tile.bg }}>
                <Icon name={tile.icon} size="lg" color={tile.color} />
              </span>
            </button>
          ))}
        </div>

        <section className="tl-section">
          <div className="tl-promo tl-reveal">
            <div className="tl-promo-card">
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8, font: '700 14px/1 var(--font-display)', color: 'var(--tide-700)' }}>
                <Icon name="waves-horizontal" size="sm" color="var(--tide-600)" />
                Tideline projects
              </span>
              <h2 style={{ font: '700 32px/1.1 var(--font-display)', letterSpacing: '-0.02em', marginTop: 12 }}>
                Clean coasts, <span style={{ color: 'var(--buoy-600)' }}>together</span>
              </h2>
              <p style={{ font: 'var(--text-body)', color: 'var(--sea-700)', marginTop: 8 }}>
                {s ? `${s.activeProjects} cleanup projects are looking for volunteers and divers right now.` : 'Cleanup projects near you are looking for volunteers and divers.'}
              </p>
              <Button iconRight="arrow-right" onClick={() => navigate('/app/cleanups')} style={{ marginTop: 16 }}>
                Join a cleanup
              </Button>
            </div>
            <div className="tl-promo-art" style={{ position: 'relative', height: '100%', minHeight: 220 }}>
              <div style={{ position: 'absolute', right: 40, top: '50%', transform: 'translateY(-50%)', display: 'grid', gridTemplateColumns: 'repeat(2, 96px)', gap: 16 }}>
                {[
                  ['hand-heart', 'var(--tide-700)'],
                  ['anchor', 'var(--sea-700)'],
                  ['users', 'var(--sea-800)'],
                  ['life-buoy', 'var(--buoy-700)'],
                ].map(([icon, color], index) => (
                  <span
                    key={icon}
                    style={{
                      width: 96,
                      height: 96,
                      borderRadius: 28,
                      background: 'rgba(255, 255, 255, 0.55)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      transform: `rotate(${index % 2 ? 6 : -6}deg)`,
                      boxShadow: '0 12px 24px rgba(140, 90, 6, 0.18)',
                    }}
                  >
                    <Icon name={icon} size={44} color={color} />
                  </span>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="tl-section" id="about">
          <div className="tl-split">
            <div className="tl-panel tl-reveal" style={{ minHeight: 320 }}>
              <CoastScene />
            </div>
            <div className="tl-panel tl-reveal" style={{ '--tl-delay': '100ms' }}>
              <div className="tl-panel-body">
                <h2 style={{ font: '700 32px/1.12 var(--font-display)', letterSpacing: '-0.02em' }}>About Tideline</h2>
                <p style={{ font: 'var(--text-body)', color: 'rgba(255, 255, 255, 0.72)' }}>
                  Tideline is a community-based ocean and coastal cleanup platform for Sri Lanka. Citizens and divers report
                  pollution, the community verifies it, administrators and the government officer approve it, and it becomes
                  a cleanup project people can join.
                </p>
                <button type="button" className="tl-pill-btn" onClick={() => document.getElementById('how-it-works')?.scrollIntoView({ behavior: 'smooth' })}>
                  Learn more <Icon name="arrow-right" size="xs" />
                </button>
              </div>
            </div>
          </div>
        </section>

        <section className="tl-section" id="impact">
          <div className="tl-split">
            <div className="tl-panel tl-reveal tl-order-last">
              <div className="tl-panel-body">
                <h2 style={{ font: '700 32px/1.12 var(--font-display)', letterSpacing: '-0.02em' }}>Our impact so far</h2>
                <p style={{ font: 'var(--text-body)', color: 'rgba(255, 255, 255, 0.72)' }}>
                  Every number here is live from the platform.
                </p>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 'var(--space-4)', marginTop: 'var(--space-2)' }}>
                  {[
                    { label: 'Open reports', value: s?.reportedSites, color: 'var(--buoy-400)' },
                    { label: 'Active projects', value: s?.activeProjects, color: 'var(--tide-300)' },
                    { label: 'Projects completed', value: s?.completedProjects, color: 'var(--sea-300)' },
                    { label: 'Volunteers', value: s?.registeredVolunteers, color: '#ff9f8a' },
                  ].map((stat) => (
                    <div key={stat.label} style={{ padding: 'var(--space-4)', borderRadius: 'var(--radius-md)', background: 'rgba(255, 255, 255, 0.04)', border: '1px solid rgba(255, 255, 255, 0.06)' }}>
                      <div style={{ font: '700 36px/1 var(--font-display)', color: stat.color }}>
                        <CountUp value={stat.value} />
                      </div>
                      <div style={{ font: 'var(--text-caption)', color: 'rgba(255, 255, 255, 0.6)', marginTop: 6 }}>{stat.label}</div>
                    </div>
                  ))}
                </div>
                {summary.error ? (
                  <span style={{ font: 'var(--text-caption)', color: 'rgba(255, 255, 255, 0.5)' }}>Live figures are unavailable right now.</span>
                ) : null}
              </div>
            </div>
            <div className="tl-panel tl-reveal tl-order-first" style={{ '--tl-delay': '100ms', minHeight: 320 }}>
              <ReefScene />
            </div>
          </div>
        </section>

        <section className="tl-section" id="how-it-works">
          <SectionTitle eyebrow="How it works" title="From a photo to a clean coast" />
          <div className="tl-steps">
            {STEPS.map((step, index) => (
              <div key={step.title} className="tl-step tl-reveal" style={{ '--tl-delay': `${index * 90}ms` }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span style={{ width: 44, height: 44, borderRadius: 12, background: 'rgba(53, 194, 183, 0.16)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <Icon name={step.icon} size="md" color="var(--tide-300)" />
                  </span>
                  <span style={{ font: '700 28px/1 var(--font-display)', color: 'rgba(255, 255, 255, 0.12)' }}>0{index + 1}</span>
                </div>
                <h3 style={{ font: '700 18px/1.25 var(--font-display)', marginTop: 'var(--space-4)' }}>{step.title}</h3>
                <p style={{ font: 'var(--text-body-sm)', color: 'rgba(255, 255, 255, 0.65)', marginTop: 6 }}>{step.body}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="tl-section" id="latest">
          <SectionTitle
            eyebrow="Latest"
            title="From the coast"
            action={
              <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
                <div className="tl-segment" role="tablist" aria-label="Latest" style={{ display: 'flex', gap: 4, padding: 4, borderRadius: 999, background: 'rgba(255, 255, 255, 0.06)' }}>
                  <button type="button" role="tab" aria-selected={latest === 'reports'} onClick={() => setLatest('reports')}>Reports</button>
                  <button type="button" role="tab" aria-selected={latest === 'projects'} onClick={() => setLatest('projects')}>Projects</button>
                </div>
                <Link to={latest === 'reports' ? '/app' : '/app/cleanups'} style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--buoy-400)', font: 'var(--text-label)', textDecoration: 'none' }}>
                  View all <Icon name="arrow-right" size="xs" />
                </Link>
              </div>
            }
          />

          <div className="tl-news" role="tabpanel">
            {(latest === 'reports' ? latestReports : latestProjects).map((item, index) => {
              const isReport = latest === 'reports';
              const image = isReport ? item.evidence?.find((e) => e.contentType?.startsWith('image/')) : null;
              return (
                <button
                  key={`${latest}-${item.id}`}
                  type="button"
                  className="tl-news-card tl-reveal"
                  style={{ '--tl-delay': `${index * 90}ms` }}
                  onClick={() => navigate(isReport ? `/app/report/${item.id}` : `/app/cleanups/${item.id}`)}
                >
                  <div className="tl-news-media">
                    {image ? <img alt="" src={mediaUrl(image.url)} style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }} /> : index % 2 ? <ReefScene /> : <CoastScene />}
                  </div>
                  <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(5,23,31,0.1) 30%, rgba(5,23,31,0.85) 100%)' }} />
                  <span style={{ position: 'absolute', top: 14, left: 14, padding: '5px 10px', borderRadius: 999, background: 'rgba(5, 23, 31, 0.65)', backdropFilter: 'blur(6px)', font: 'var(--text-micro)', color: 'var(--white)' }}>
                    {formatDate(item.createdAt)}
                  </span>
                  <div style={{ position: 'absolute', left: 14, right: 14, bottom: 14, padding: '12px 14px', borderRadius: 12, background: 'rgba(5, 23, 31, 0.6)', backdropFilter: 'blur(8px)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <span style={{ display: 'block', font: '600 12px/1.2 var(--font-mono)', color: 'var(--tide-300)' }}>
                      {item.reference} · {isReport ? `${SEVERITY_LABEL[item.severity]} severity` : `${item.completionPercentage}% complete`}
                    </span>
                    <span style={{ display: 'block', font: '700 15px/1.3 var(--font-display)', marginTop: 4, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {item.title}
                    </span>
                    <span style={{ display: 'block', font: 'var(--text-caption)', color: 'rgba(255,255,255,0.65)', marginTop: 2 }}>{locationLine(item)}</span>
                  </div>
                </button>
              );
            })}
            {!(latest === 'reports' ? latestReports : latestProjects).length ? (
              <p style={{ font: 'var(--text-body)', color: 'rgba(255, 255, 255, 0.6)' }}>Nothing here yet.</p>
            ) : null}
          </div>
        </section>
      </div>

      <section className="tl-section">
        <div className="tl-wrap">
          <div className="tl-reveal" style={{ borderRadius: 24, padding: 'var(--space-10, 40px)', background: 'linear-gradient(120deg, var(--tide-700), var(--sea-600))', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 24, flexWrap: 'wrap' }}>
            <div>
              <h2 style={{ font: '700 32px/1.12 var(--font-display)', letterSpacing: '-0.02em' }}>Seen pollution on the coast?</h2>
              <p style={{ font: 'var(--text-body-lg)', color: 'rgba(255,255,255,0.8)', marginTop: 6 }}>It takes a minute to report, and your neighbours can help confirm it.</p>
            </div>
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
              <Button size="lg" variant="inverse" iconLeft="camera" onClick={() => goTo('/app/submit', true)}>Report a polluted site</Button>
              {!user ? <Button size="lg" variant="secondary" onClick={() => navigate('/register')}>Create an account</Button> : null}
            </div>
          </div>
        </div>
      </section>

      <footer className="tl-footer" style={{ marginTop: 'var(--space-16)', background: '#03121a', borderTop: '1px solid rgba(255,255,255,0.06)' }}>
        <div className="tl-wrap" style={{ padding: 'var(--space-12) var(--space-6) var(--space-6)' }}>
          <div className="tl-footer-cols">
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <Icon name="waves-horizontal" size="lg" color="var(--tide-300)" />
                <span style={{ font: '700 22px/1 var(--font-display)', letterSpacing: '-0.03em' }}>Tideline</span>
              </div>
              <p style={{ font: 'var(--text-body-sm)', color: 'rgba(255,255,255,0.55)', marginTop: 12, maxWidth: 320 }}>
                Community-based ocean and coastal cleanup management for Sri Lanka.
              </p>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <span style={{ font: 'var(--text-label)' }}>Community</span>
              <Link to="/app">Reports</Link>
              <Link to="/app/cleanups">Cleanup projects</Link>
              <Link to="/app/opportunities">Diver opportunities</Link>
              <Link to="/app/submit">Report pollution</Link>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <span style={{ font: 'var(--text-label)' }}>Officials</span>
              <Link to="/console">Review queue</Link>
              <Link to="/console/projects">Projects</Link>
              <Link to="/console/analytics">Analytics</Link>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              <span style={{ font: 'var(--text-label)' }}>Account</span>
              <Link to="/login">Sign in</Link>
              <Link to="/register">Create an account</Link>
              <Link to="/forgot-password">Forgot password</Link>
            </div>
          </div>
          <div style={{ marginTop: 'var(--space-10, 40px)', paddingTop: 'var(--space-5)', borderTop: '1px solid rgba(255,255,255,0.08)', display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap', font: 'var(--text-caption)', color: 'rgba(255,255,255,0.45)' }}>
            <span>© {new Date().getFullYear()} Tideline</span>
            <span>Marine Environment Protection Authority partners</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
