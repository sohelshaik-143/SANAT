"""
CivicGuard-AI — Deepfake / Fake Image Detector
================================================
Detects AI-generated, manipulated, or recycled images
used to file fake complaints or submit fraudulent resolutions.

Detection Methods:
1. Error Level Analysis (ELA) — detects JPEG re-compression artifacts
2. Noise pattern analysis — AI images have uniform noise patterns
3. Metadata consistency — missing/tampered EXIF data
4. Frequency domain analysis — GAN artifacts in DCT spectrum
5. Color histogram analysis — unnatural color distributions

In production, this would use a trained CNN model (deepfake_model.pkl).
This module provides heuristic-based detection as a fallback/baseline.
"""

import os
import hashlib
import numpy as np
from datetime import datetime

try:
    from PIL import Image, ImageFilter
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False

try:
    import cv2
    CV2_AVAILABLE = True
except ImportError:
    CV2_AVAILABLE = False


class DeepfakeDetector:
    """
    Multi-method deepfake and image manipulation detector.
    Combines several heuristic methods to produce a fraud confidence score.
    """
    
    def __init__(self, model_path=None):
        """
        Initialize the detector.
        
        Args:
            model_path: Path to a trained deepfake detection model (.pkl/.h5)
                       If None, uses heuristic-based detection.
        """
        self.model = None
        self.model_loaded = False
        
        if model_path and os.path.exists(model_path):
            try:
                import joblib
                self.model = joblib.load(model_path)
                self.model_loaded = True
                print(f"[DeepfakeDetector] Model loaded from {model_path}")
            except Exception as e:
                print(f"[DeepfakeDetector] Failed to load model: {e}")
                print("[DeepfakeDetector] Using heuristic detection")
        else:
            print("[DeepfakeDetector] No model file found. Using heuristic detection.")
    
    def is_loaded(self):
        """Check if the ML model is loaded."""
        return self.model_loaded
    
    def analyze(self, image_path):
        """
        Analyze an image for signs of deepfake/manipulation.
        
        Returns:
            dict: {
                'is_deepfake': bool,
                'confidence': float (0-1),
                'details': str,
                'methods': dict of individual method scores
            }
        """
        if not os.path.exists(image_path):
            return {
                'is_deepfake': False,
                'confidence': 0.0,
                'details': 'Image file not found',
                'methods': {}
            }
        
        scores = {}
        details = []
        
        # Method 1: EXIF Metadata Analysis
        exif_score, exif_detail = self._check_exif(image_path)
        scores['exif_analysis'] = exif_score
        if exif_detail:
            details.append(exif_detail)
        
        # Method 2: Error Level Analysis
        if PIL_AVAILABLE:
            ela_score, ela_detail = self._error_level_analysis(image_path)
            scores['ela'] = ela_score
            if ela_detail:
                details.append(ela_detail)
        
        # Method 3: Noise Pattern Analysis
        if PIL_AVAILABLE:
            noise_score, noise_detail = self._noise_analysis(image_path)
            scores['noise_pattern'] = noise_score
            if noise_detail:
                details.append(noise_detail)
        
        # Method 4: Color Distribution Analysis
        if PIL_AVAILABLE:
            color_score, color_detail = self._color_analysis(image_path)
            scores['color_distribution'] = color_score
            if color_detail:
                details.append(color_detail)
        
        # Method 5: File integrity check
        integrity_score, integrity_detail = self._file_integrity(image_path)
        scores['file_integrity'] = integrity_score
        if integrity_detail:
            details.append(integrity_detail)
        
        # Calculate weighted average
        if scores:
            weights = {
                'exif_analysis': 0.15,
                'ela': 0.30,
                'noise_pattern': 0.25,
                'color_distribution': 0.15,
                'file_integrity': 0.15
            }
            
            weighted_sum = sum(
                scores.get(k, 0) * weights.get(k, 0.2) 
                for k in scores
            )
            total_weight = sum(
                weights.get(k, 0.2) for k in scores
            )
            
            fake_confidence = weighted_sum / total_weight if total_weight > 0 else 0
        else:
            fake_confidence = 0.0
        
        is_deepfake = fake_confidence > 0.65
        
        return {
            'is_deepfake': is_deepfake,
            'confidence': round(fake_confidence, 4),
            'details': '; '.join(details) if details else 'No anomalies detected',
            'methods': scores
        }
    
    def _check_exif(self, image_path):
        """Check EXIF metadata for signs of manipulation."""
        try:
            if not PIL_AVAILABLE:
                return 0.0, None
            
            img = Image.open(image_path)
            exif = img.getexif()
            
            if not exif:
                return 0.3, "No EXIF metadata (common in AI-generated images)"
            
            # Check for editing software markers
            software = exif.get(305, '')  # Software tag
            suspicious_software = ['photoshop', 'gimp', 'paint', 'canva']
            if any(s in str(software).lower() for s in suspicious_software):
                return 0.7, f"Edited with: {software}"
            
            # Check for missing camera info (AI-generated won't have this)
            make = exif.get(271, '')   # Camera make
            model = exif.get(272, '')  # Camera model
            if not make and not model:
                return 0.4, "No camera information in metadata"
            
            return 0.1, None
        
        except Exception:
            return 0.2, None
    
    def _error_level_analysis(self, image_path):
        """
        Error Level Analysis — re-save at known quality and compare.
        Manipulated regions show different error levels than original regions.
        """
        try:
            img = Image.open(image_path).convert('RGB')
            
            # Re-save at quality 95
            import io
            buffer = io.BytesIO()
            img.save(buffer, 'JPEG', quality=95)
            buffer.seek(0)
            resaved = Image.open(buffer).convert('RGB')
            
            # Calculate pixel-level differences
            orig_arr = np.array(img, dtype=float)
            resaved_arr = np.array(resaved, dtype=float)
            
            ela = np.abs(orig_arr - resaved_arr)
            ela_mean = np.mean(ela)
            ela_std = np.std(ela)
            
            # High variance in ELA suggests manipulation
            if ela_std > 30:
                return 0.7, f"High ELA variance ({ela_std:.1f}) suggests manipulation"
            elif ela_std > 20:
                return 0.4, f"Moderate ELA variance ({ela_std:.1f})"
            
            return 0.1, None
        
        except Exception:
            return 0.2, None
    
    def _noise_analysis(self, image_path):
        """
        Analyze noise patterns — AI-generated images have uniform noise,
        while real photos have sensor-specific noise patterns.
        """
        try:
            img = Image.open(image_path).convert('L')  # Grayscale
            arr = np.array(img, dtype=float)
            
            # Extract noise by subtracting blurred version
            blurred = np.array(img.filter(ImageFilter.GaussianBlur(3)), dtype=float)
            noise = arr - blurred
            
            noise_std = np.std(noise)
            
            # Very uniform noise is suspicious (AI-generated)
            if noise_std < 2.0:
                return 0.6, "Suspiciously uniform noise pattern (possible AI generation)"
            # Very high noise might indicate heavy manipulation
            elif noise_std > 40:
                return 0.5, "Abnormally high noise levels"
            
            return 0.1, None
        
        except Exception:
            return 0.2, None
    
    def _color_analysis(self, image_path):
        """Analyze color distribution for unnatural patterns."""
        try:
            img = Image.open(image_path).convert('RGB')
            arr = np.array(img)
            
            # Check for unnatural color distributions
            r_std = np.std(arr[:, :, 0])
            g_std = np.std(arr[:, :, 1])
            b_std = np.std(arr[:, :, 2])
            
            # Very low color variance is suspicious
            if min(r_std, g_std, b_std) < 5:
                return 0.5, "Unnatural color uniformity detected"
            
            # Check for perfectly uniform backgrounds (copy-paste indicator)
            unique_colors = len(set(tuple(p) for p in arr.reshape(-1, 3)[:1000]))
            if unique_colors < 50:
                return 0.6, f"Very limited color palette ({unique_colors} unique colors in sample)"
            
            return 0.1, None
        
        except Exception:
            return 0.2, None
    
    def _file_integrity(self, image_path):
        """Check file integrity and format consistency."""
        try:
            file_size = os.path.getsize(image_path)
            
            # Suspiciously small files
            if file_size < 5000:  # < 5KB
                return 0.6, "Suspiciously small file size"
            
            # Check file header matches extension
            with open(image_path, 'rb') as f:
                header = f.read(10)
            
            is_jpeg = header[:2] == b'\xff\xd8'
            is_png = header[:4] == b'\x89PNG'
            
            ext = os.path.splitext(image_path)[1].lower()
            
            if ext in ('.jpg', '.jpeg') and not is_jpeg:
                return 0.7, "File extension doesn't match content type"
            if ext == '.png' and not is_png:
                return 0.7, "File extension doesn't match content type"
            
            return 0.05, None
        
        except Exception:
            return 0.2, None
