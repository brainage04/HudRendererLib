import bpy, json, os
from pathlib import Path
ROOT = Path(__file__).resolve().parent
bpy.ops.wm.open_mainfile(filepath=str(ROOT/'assets/reference-head.blend'))
bpy.context.view_layer.update()
scene = bpy.context.scene
rows = []
for o in scene.objects:
    r = {'name':o.name,'type':o.type,'location':list(o.location),'scale':list(o.scale),'rotation':list(o.rotation_euler),'parent':o.parent.name if o.parent else None,'dimensions':list(o.dimensions),'properties':{k:str(v) for k,v in o.items()}}
    if o.type == 'MESH':
        r.update(vertices=len(o.data.vertices),polygons=len(o.data.polygons),materials=[m.name for m in o.data.materials],local_bounds=[[min(v.co[i] for v in o.data.vertices),max(v.co[i] for v in o.data.vertices)] for i in range(3)])
    rows.append(r)
report={'objects':rows,'camera':scene.camera.name,'camera_rotation':list(scene.camera.rotation_euler),'camera_matrix':[list(r) for r in scene.camera.matrix_world],'view_transform':scene.view_settings.view_transform,'look':scene.view_settings.look,'world':scene.world.name,'images':[{'name':i.name,'size':list(i.size),'packed':bool(i.packed_file)} for i in bpy.data.images]}
(ROOT/'evidence/reference-inspection.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
