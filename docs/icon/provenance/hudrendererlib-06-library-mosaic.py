from pathlib import Path
import sys
sys.path.insert(0,str(Path(__file__).resolve().parent))
from scene import render
render('06-library-mosaic')
