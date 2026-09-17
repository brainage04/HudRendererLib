"""Reopen every packed scene and independently inspect actual saved geometry and assets."""
import bpy
import hashlib
import json
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT))
import scene as author
reports=[]
for kind in author.ARRANGEMENTS:
    stem='hudrendererlib-'+kind
    bpy.ops.wm.open_mainfile(filepath=str(ROOT/(stem+'.blend')))
    scene=bpy.context.scene
    assert scene.render.engine=='CYCLES' and scene.cycles.device=='CPU'
    assert scene.cycles.samples==64 and scene.render.threads==2
    assert scene.render.resolution_x==scene.render.resolution_y==1024
    author.HEADS=[]
    for obj in scene.objects:
        if obj.get('role')=='owner_head':
            layers=sorted([c for c in obj.children if c.type=='MESH'],key=lambda c:0 if c.get('skin_layer')=='base' else 1)
            author.HEADS.append((obj,layers))
    author.SHELVES=[o for o in scene.objects if o.get('role')=='vanilla_bookshelf']
    author.SUBJECTS=author.SHELVES+[o for p,layers in author.HEADS for o in layers]
    report=author.verify_scene()
    files=[i for i in bpy.data.images if i.source=='FILE']
    assert len(files)==3 and all(i.packed_file for i in files)
    actual={hashlib.sha256(bytes(i.packed_file.data)).hexdigest() for i in files}
    expected={hashlib.sha256((ROOT/'assets'/f).read_bytes()).hexdigest() for f in ['supplied-skin.png','bookshelf.png','oak_planks.png']}
    assert actual==expected
    for mat in bpy.data.materials:
        if mat.use_nodes:
            for node in mat.node_tree.nodes:
                if node.type=='TEX_IMAGE' and node.image:
                    assert node.interpolation=='Closest'
    assert bpy.data.texts['scene.py'].as_string()==(ROOT/'scene.py').read_text()
    assert bpy.data.texts[stem+'.py'].as_string()==(ROOT/(stem+'.py')).read_text()
    assert len([o for o in scene.objects if o.type=='FONT'])==0
    assert not any('PLUS' in o.name or 'Plus' in o.name for o in scene.objects)
    metadata=json.loads((ROOT/(stem+'-metadata.json')).read_text())
    assert metadata['png_sha256']==hashlib.sha256((ROOT/(stem+'.png')).read_bytes()).hexdigest()
    report.update(name=stem,reopened_from_disk=True,packed_assets_exactly_match_originals=True,embedded_script_matches_saved_script=True,no_text_or_plus_symbol=True,blender_version=bpy.app.version_string)
    reports.append(report)
(ROOT/'saved-scene-verification.json').write_text(json.dumps(reports,indent=2)+'\n')
print(json.dumps({'reopened_packed_scenes':len(reports),'geometry_and_uv_checks':'PASS','assets':'All 3 textures exactly match original bytes in each saved scene','orientations':'All heads match the NMSR reference'},indent=2))
