// Generates launcher PNG icons for Narcic DOH (shield + N glyph, cyan→violet gradient).
// Run: node tools/gen_icons.mjs
import { deflateSync } from 'node:zlib';
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..');
const RES = join(ROOT, 'app', 'src', 'main', 'res');

// ---------- PNG encoder ----------
const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
function crc32(buf) {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
  const out = Buffer.alloc(8 + data.length + 4);
  out.writeUInt32BE(data.length, 0);
  out.write(type, 4, 'ascii');
  data.copy(out, 8);
  out.writeUInt32BE(crc32(Buffer.concat([Buffer.from(type, 'ascii'), data])), 8 + data.length);
  return out;
}
function encodePNG(width, height, rgba) {
  const sig = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
  const stride = width * 4;
  const raw = Buffer.alloc((stride + 1) * height);
  for (let y = 0; y < height; y++) {
    raw[y * (stride + 1)] = 0;
    rgba.copy(raw, y * (stride + 1) + 1, y * stride, (y + 1) * stride);
  }
  return Buffer.concat([sig, chunk('IHDR', ihdr), chunk('IDAT', deflateSync(raw, { level: 9 })), chunk('IEND', Buffer.alloc(0))]);
}

// ---------- geometry (24-space, matches vector drawables) ----------
function cubic(p0, p1, p2, p3, t) {
  const u = 1 - t;
  return [
    u*u*u*p0[0] + 3*u*u*t*p1[0] + 3*u*t*t*p2[0] + t*t*t*p3[0],
    u*u*u*p0[1] + 3*u*u*t*p1[1] + 3*u*t*t*p2[1] + t*t*t*p3[1],
  ];
}
function shieldPolygon() {
  const pts = [[12, 2.4]];
  pts.push([18.6, 4.9]);
  for (let i = 1; i <= 10; i++) pts.push(cubic([18.6,4.9],[19.1,5.1],[19.5,5.6],[19.5,6.2], i/10));
  pts.push([19.5, 11.1]);
  for (let i = 1; i <= 16; i++) pts.push(cubic([19.5,11.1],[19.5,15.8],[16.4,19.6],[12,21.6], i/16));
  for (let i = 1; i <= 16; i++) pts.push(cubic([12,21.6],[7.6,19.6],[4.5,15.8],[4.5,11.1], i/16));
  pts.push([4.5, 6.2]);
  for (let i = 1; i <= 10; i++) pts.push(cubic([4.5,6.2],[4.5,5.6],[4.9,5.1],[5.4,4.9], i/10));
  return pts;
}
const SHIELD = shieldPolygon();
const N_SEGS = [
  [[8.9, 15.6], [8.9, 8.4]],
  [[8.9, 8.4], [15.1, 15.6]],
  [[15.1, 15.6], [15.1, 8.4]],
];
const N_WIDTH = 1.6;

function insidePolygon(x, y, poly) {
  let inside = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if (yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside;
  }
  return inside;
}
function distToSeg(px, py, [a, b]) {
  const dx = b[0] - a[0], dy = b[1] - a[1];
  const t = Math.max(0, Math.min(1, ((px - a[0]) * dx + (py - a[1]) * dy) / (dx*dx + dy*dy)));
  return Math.hypot(px - (a[0] + t*dx), py - (a[1] + t*dy));
}
function roundedRectSDF(x, y, size, r) {
  const c = size / 2;
  const qx = Math.abs(x - c) - (c - r), qy = Math.abs(y - c) - (c - r);
  const ox = Math.max(qx, 0), oy = Math.max(qy, 0);
  return Math.hypot(ox, oy) + Math.min(Math.max(qx, qy), 0) - r;
}

// gradient cyan -> violet over shield bbox (24-space)
const C1 = [0x22, 0xd3, 0xee], C2 = [0x8b, 0x5c, 0xf6];
const G0 = [4.5, 2.4], G1 = [19.5, 21.6];
const GD = [G1[0]-G0[0], G1[1]-G0[1]];
const GD2 = GD[0]*GD[0] + GD[1]*GD[1];
function gradColor(x, y) {
  let t = ((x - G0[0]) * GD[0] + (y - G0[1]) * GD[1]) / GD2;
  t = Math.max(0, Math.min(1, t));
  return [0, 1, 2].map(i => Math.round(C1[i] + (C2[i] - C1[i]) * t));
}

// background: navy vertical gradient
const B0 = [0x0a, 0x0f, 0x1e], B1 = [0x11, 0x1b, 0x33];
function bgColor(size, y) {
  const t = y / size;
  return [0, 1, 2].map(i => Math.round(B0[i] + (B1[i] - B0[i]) * t));
}

function renderIcon(size, opts) {
  const { shape, shieldScale = 0.52 } = opts;
  const SS = 3;
  const k = (size * shieldScale) / 15; // 24-space half-width 7.5 -> shieldScale*size/2
  const off = size / 2;
  const map = ([x, y]) => [(x - 12) * k + off, (y - 12) * k + off];
  const shieldPx = SHIELD.map(map);
  const segsPx = N_SEGS.map(([a, b]) => [map(a), map(b)]);
  const nW = N_WIDTH * k;
  const g0px = map(G0), g1px = map(G1);
  const gdx = g1px[0] - g0px[0], gdy = g1px[1] - g0px[1];
  const gd2 = gdx*gdx + gdy*gdy;
  const r = size * (shape === 'circle' ? 0.5 : 0.18);

  const rgba = Buffer.alloc(size * size * 4);
  for (let py = 0; py < size; py++) {
    for (let px = 0; px < size; px++) {
      let rAcc = 0, gAcc = 0, bAcc = 0, aAcc = 0;
      for (let sy = 0; sy < SS; sy++) {
        for (let sx = 0; sx < SS; sx++) {
          const x = px + (sx + 0.5) / SS;
          const y = py + (sy + 0.5) / SS;
          // background coverage
          const bgSd = shape === 'circle' ? Math.hypot(x - off, y - off) - r : roundedRectSDF(x, y, size, r);
          let cov = Math.max(0, Math.min(1, 0.5 - bgSd * SS));
          if (cov <= 0) continue;
          const bg = bgColor(size, y);
          let rC = bg[0], gC = bg[1], bC = bg[2];
          // N glyph (on top of shield / background)
          let onN = false;
          for (const [a, b] of segsPx) {
            if (distToSeg(x, y, [a, b]) < nW / 2) { onN = true; break; }
          }
          if (onN) {
            rC = 255; gC = 255; bC = 255;
          } else if (insidePolygon(x, y, shieldPx)) {
            let t = ((x - g0px[0]) * gdx + (y - g0px[1]) * gdy) / gd2;
            t = Math.max(0, Math.min(1, t));
            [rC, gC, bC] = [0,1,2].map(i => Math.round(C1[i] + (C2[i] - C1[i]) * t));
          }
          rAcc += rC * cov; gAcc += gC * cov; bAcc += bC * cov; aAcc += cov;
        }
      }
      const i = (py * size + px) * 4;
      if (aAcc > 0) {
        rgba[i]     = Math.round(rAcc / aAcc);
        rgba[i + 1] = Math.round(gAcc / aAcc);
        rgba[i + 2] = Math.round(bAcc / aAcc);
        rgba[i + 3] = Math.round((aAcc / (SS * SS)) * 255);
      }
    }
  }
  return encodePNG(size, size, rgba);
}

const DENSITIES = [
  ['mipmap-mdpi', 48], ['mipmap-hdpi', 72], ['mipmap-xhdpi', 96],
  ['mipmap-xxhdpi', 144], ['mipmap-xxxhdpi', 192],
];
for (const [dir, s] of DENSITIES) {
  const d = join(RES, dir);
  mkdirSync(d, { recursive: true });
  writeFileSync(join(d, 'ic_launcher.png'), renderIcon(s, { shape: 'roundrect' }));
  writeFileSync(join(d, 'ic_launcher_round.png'), renderIcon(s, { shape: 'circle' }));
}
mkdirSync(join(RES, '..'), { recursive: true });
writeFileSync(join(RES, '..', 'ic_launcher-playstore.png'), renderIcon(512, { shape: 'roundrect', shieldScale: 0.56 }));
console.log('icons generated');
