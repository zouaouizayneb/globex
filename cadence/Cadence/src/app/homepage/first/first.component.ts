import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-first',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './first.component.html',
  styleUrls: ['./first.component.css']
})
export class FirstComponent implements OnInit {

  loaded = false;

  /**
   * ─── CHOOSE YOUR VIDEO ───────────────────────────────────────────────────
   *
   * Pick ONE of the options below and paste it as the value of `videoUrl`.
   *
   * OPTION A — Boutique fashion / clothing rack (warm tones, cinematic)
   *   https://videos.pexels.com/video-files/853800/853800-hd_1920_1080_25fps.mp4
   *   Preview: https://www.pexels.com/video/woman-picking-out-clothes-853800/
   *
   * OPTION B — Luxury lifestyle / city + products montage (dark, editorial)
   *   https://videos.pexels.com/video-files/3843436/3843436-hd_1280_720_24fps.mp4
   *   Preview: https://www.pexels.com/video/aerial-footage-of-a-city-at-night-3843436/
   *
   * OPTION C — Online shopping / parcel unboxing (clean, e-commerce native)
   *   https://videos.pexels.com/video-files/6612460/6612460-hd_1920_1080_25fps.mp4
   *   Preview: https://www.pexels.com/video/delivering-bags-6612460/
   *
   * OPTION D — Aerial city + global trade (international vibe, dramatic)
   *   https://videos.pexels.com/video-files/3252186/3252186-hd_1280_720_30fps.mp4
   *   Preview: https://www.pexels.com/video/aerial-view-of-a-busy-city-3252186/
   *
   * ─────────────────────────────────────────────────────────────────────────
   * NOTE: All videos are free for commercial use (Pexels CC0 license).
   * The green overlay in the CSS will harmonize any of these with your brand.
   * ─────────────────────────────────────────────────────────────────────────
   */
  videoUrl = 'https://videos.pexels.com/video-files/3252186/3252186-hd_1280_720_30fps.mp4';

  categories = [
    { icon: '', label: 'Home Décor' },
    { icon: '', label: 'Clothing' },
    { icon: '', label: 'Sports' },
    { icon: '', label: 'School' },
    { icon: '', label: 'International Shipping' },
  ];

  stats = [
    { value: '150+', label: 'Countries' },
    { value: '10k+', label: 'Products' },
    { value: '4.9★', label: 'Rating' },
  ];

  ngOnInit(): void {
    requestAnimationFrame(() => {
      setTimeout(() => { this.loaded = true; }, 80);
    });
  }
}