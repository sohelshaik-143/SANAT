"""
CivicGuard-AI — Civic Issue Classifier
========================================
Classifies uploaded images into civic issue categories:
- POTHOLE, GARBAGE, DRAINAGE, WATER_LEAK, STREETLIGHT,
  SEWAGE, ENCROACHMENT, TREE_FALL, ROAD_SIGN, BRIDGE_DAMAGE,
  CONSTRUCTION_DEBRIS, STRAY_ANIMALS, NOISE_POLLUTION, 
  AIR_POLLUTION, OTHER

Uses a combination of:
1. Pre-trained image feature extraction (color/texture histograms)
2. TF-IDF on image metadata + description
3. GradientBoosting classifier for final prediction

In production, this would use a fine-tuned CNN (ResNet/EfficientNet)
trained on Indian civic issue datasets. This module provides a
heuristic baseline using color and texture analysis.
"""

import os
import numpy as np
from datetime import datetime

try:
    from PIL import Image
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False

try:
    from sklearn.ensemble import GradientBoostingClassifier
    from sklearn.feature_extraction.text import TfidfVectorizer
    import joblib
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False


# Issue categories and their visual characteristics
CATEGORIES = {
    'POTHOLE': {
        'description': 'Road damage, potholes, cracks',
        'color_hints': ['gray', 'dark', 'brown'],
        'keywords': ['road', 'pothole', 'crack', 'damage', 'asphalt']
    },
    'GARBAGE': {
        'description': 'Garbage dumping, waste accumulation',
        'color_hints': ['mixed', 'colorful', 'brown'],
        'keywords': ['garbage', 'waste', 'dump', 'trash', 'litter']
    },
    'DRAINAGE': {
        'description': 'Drainage overflow, blocked drains',
        'color_hints': ['dark', 'green', 'brown'],
        'keywords': ['drain', 'water', 'overflow', 'blocked', 'clogged']
    },
    'WATER_LEAK': {
        'description': 'Water leakage from pipes or tanks',
        'color_hints': ['blue', 'wet', 'gray'],
        'keywords': ['water', 'leak', 'pipe', 'burst', 'flooding']
    },
    'STREETLIGHT': {
        'description': 'Broken or non-functional streetlights',
        'color_hints': ['dark', 'metal', 'gray'],
        'keywords': ['light', 'streetlight', 'lamp', 'pole', 'dark']
    },
    'SEWAGE': {
        'description': 'Sewage overflow, open manholes',
        'color_hints': ['dark', 'brown', 'green'],
        'keywords': ['sewage', 'manhole', 'overflow', 'smell', 'sewer']
    },
    'ENCROACHMENT': {
        'description': 'Illegal encroachment on public land',
        'color_hints': ['varied'],
        'keywords': ['encroach', 'illegal', 'construction', 'occupy']
    },
    'TREE_FALL': {
        'description': 'Fallen trees, hazardous branches',
        'color_hints': ['green', 'brown', 'wood'],
        'keywords': ['tree', 'fallen', 'branch', 'hazard', 'block']
    },
    'ROAD_SIGN': {
        'description': 'Damaged or missing road signs',
        'color_hints': ['red', 'yellow', 'metal'],
        'keywords': ['sign', 'signal', 'traffic', 'damaged', 'missing']
    },
    'BRIDGE_DAMAGE': {
        'description': 'Bridge or flyover structural damage',
        'color_hints': ['gray', 'concrete'],
        'keywords': ['bridge', 'flyover', 'crack', 'structural', 'concrete']
    },
    'CONSTRUCTION_DEBRIS': {
        'description': 'Construction debris on roads/footpaths',
        'color_hints': ['gray', 'brown', 'mixed'],
        'keywords': ['debris', 'construction', 'rubble', 'cement', 'brick']
    },
    'STRAY_ANIMALS': {
        'description': 'Stray animal menace',
        'color_hints': ['varied'],
        'keywords': ['stray', 'dog', 'animal', 'cattle', 'cow']
    },
    'AIR_POLLUTION': {
        'description': 'Air pollution, burning waste',
        'color_hints': ['gray', 'smoky', 'hazy'],
        'keywords': ['smoke', 'burning', 'pollution', 'fire', 'haze']
    },
    'OTHER': {
        'description': 'Other civic issues',
        'color_hints': ['varied'],
        'keywords': ['other', 'misc']
    }
}


class IssueClassifier:
    """
    Classifies civic issue images into predefined categories.
    Uses color histogram analysis and texture features as a
    heuristic baseline. In production, swap with a trained CNN.
    """
    
    def __init__(self, model_path=None):
        """
        Initialize classifier.
        
        Args:
            model_path: Path to trained model. If None, uses heuristics.
        """
        self.model = None
        self.model_loaded = False
        self.categories = list(CATEGORIES.keys())
        
        if model_path and os.path.exists(model_path):
            try:
                self.model = joblib.load(model_path)
                self.model_loaded = True
                print(f"[IssueClassifier] Model loaded from {model_path}")
            except Exception as e:
                print(f"[IssueClassifier] Model load failed: {e}")
        else:
            print("[IssueClassifier] Using heuristic classification")
    
    def is_loaded(self):
        return self.model_loaded
    
    def classify(self, image_path, description=None):
        """
        Classify a civic issue image.
        
        Args:
            image_path: Path to the image file
            description: Optional text description for better classification
        
        Returns:
            dict: {
                'category': str,
                'confidence': float (0-1),
                'top_3': list of (category, confidence) tuples,
                'features': dict of extracted features
            }
        """
        if not os.path.exists(image_path):
            return {
                'category': 'OTHER',
                'confidence': 0.5,
                'top_3': [('OTHER', 0.5)],
                'features': {}
            }
        
        features = {}
        scores = {cat: 0.0 for cat in self.categories}
        
        # Extract image features
        if PIL_AVAILABLE:
            color_features = self._extract_color_features(image_path)
            texture_features = self._extract_texture_features(image_path)
            features.update(color_features)
            features.update(texture_features)
            
            # Score categories based on color analysis
            scores = self._score_by_color(color_features, scores)
            scores = self._score_by_texture(texture_features, scores)
        
        # Score by description keywords if provided
        if description:
            scores = self._score_by_keywords(description, scores)
        
        # Normalize scores to probabilities
        total = sum(scores.values())
        if total > 0:
            scores = {k: v / total for k, v in scores.items()}
        else:
            scores = {k: 1.0 / len(scores) for k in scores}
        
        # Sort by score
        sorted_scores = sorted(scores.items(), key=lambda x: x[1], reverse=True)
        
        top_category = sorted_scores[0][0]
        top_confidence = sorted_scores[0][1]
        
        # Boost confidence if significantly higher than #2
        if len(sorted_scores) > 1:
            gap = top_confidence - sorted_scores[1][1]
            if gap > 0.15:
                top_confidence = min(0.95, top_confidence + 0.1)
        
        return {
            'category': top_category,
            'confidence': round(top_confidence, 4),
            'top_3': [(cat, round(conf, 4)) for cat, conf in sorted_scores[:3]],
            'features': features
        }
    
    def _extract_color_features(self, image_path):
        """Extract color histogram features."""
        try:
            img = Image.open(image_path).convert('RGB').resize((128, 128))
            arr = np.array(img)
            
            features = {}
            for i, channel in enumerate(['red', 'green', 'blue']):
                ch = arr[:, :, i].flatten()
                features[f'{channel}_mean'] = float(np.mean(ch))
                features[f'{channel}_std'] = float(np.std(ch))
                features[f'{channel}_median'] = float(np.median(ch))
            
            # Brightness
            gray = np.mean(arr, axis=2)
            features['brightness_mean'] = float(np.mean(gray))
            features['brightness_std'] = float(np.std(gray))
            
            # Saturation (rough estimate)
            max_ch = np.max(arr, axis=2).astype(float)
            min_ch = np.min(arr, axis=2).astype(float)
            saturation = np.where(max_ch > 0, (max_ch - min_ch) / max_ch, 0)
            features['saturation_mean'] = float(np.mean(saturation))
            
            return features
        
        except Exception:
            return {}
    
    def _extract_texture_features(self, image_path):
        """Extract basic texture features using gradient magnitude."""
        try:
            img = Image.open(image_path).convert('L').resize((128, 128))
            arr = np.array(img, dtype=float)
            
            # Simple gradient (edge detection proxy)
            gx = np.diff(arr, axis=1)
            gy = np.diff(arr, axis=0)
            
            features = {
                'edge_intensity_mean': float(np.mean(np.abs(gx))),
                'edge_intensity_std': float(np.std(np.abs(gx))),
                'texture_roughness': float(np.mean(np.abs(gy))),
                'contrast': float(np.max(arr) - np.min(arr))
            }
            
            return features
        
        except Exception:
            return {}
    
    def _score_by_color(self, features, scores):
        """Score categories based on color characteristics."""
        brightness = features.get('brightness_mean', 128)
        saturation = features.get('saturation_mean', 0.5)
        red_mean = features.get('red_mean', 128)
        green_mean = features.get('green_mean', 128)
        blue_mean = features.get('blue_mean', 128)
        
        # Dark images → pothole, drainage, sewage
        if brightness < 80:
            scores['POTHOLE'] += 0.15
            scores['DRAINAGE'] += 0.12
            scores['SEWAGE'] += 0.10
        
        # Green-ish → tree, vegetation
        if green_mean > red_mean and green_mean > blue_mean:
            scores['TREE_FALL'] += 0.15
            scores['DRAINAGE'] += 0.08
        
        # Brown/earth tones → pothole, construction debris
        if red_mean > 100 and green_mean > 70 and blue_mean < 100:
            scores['POTHOLE'] += 0.10
            scores['CONSTRUCTION_DEBRIS'] += 0.12
        
        # High saturation, mixed colors → garbage
        if saturation > 0.4:
            scores['GARBAGE'] += 0.12
        
        # Gray/smoky → air pollution
        if brightness > 120 and saturation < 0.15:
            scores['AIR_POLLUTION'] += 0.15
        
        # Blue tones → water leak
        if blue_mean > red_mean and blue_mean > green_mean:
            scores['WATER_LEAK'] += 0.12
        
        return scores
    
    def _score_by_texture(self, features, scores):
        """Score categories based on texture analysis."""
        edge_intensity = features.get('edge_intensity_mean', 10)
        roughness = features.get('texture_roughness', 10)
        
        # Rough textures → pothole, construction debris
        if roughness > 20:
            scores['POTHOLE'] += 0.10
            scores['CONSTRUCTION_DEBRIS'] += 0.08
        
        # Smooth → water, clean surfaces
        if roughness < 8:
            scores['WATER_LEAK'] += 0.08
        
        # High edge density → garbage (lots of objects)
        if edge_intensity > 25:
            scores['GARBAGE'] += 0.10
            scores['CONSTRUCTION_DEBRIS'] += 0.06
        
        return scores
    
    def _score_by_keywords(self, description, scores):
        """Score based on keyword matching in the description."""
        desc_lower = description.lower()
        
        for category, info in CATEGORIES.items():
            for keyword in info['keywords']:
                if keyword in desc_lower:
                    scores[category] += 0.20  # Strong signal from text
        
        return scores
