"""HudRendererLib: six combinations of an unchanged Minecraft skin head and bookshelf cubes.
Run only through run-blender.py. No GUI, display, audio or GPU. All assets are local and packed.
"""
import bpy
import hashlib
import json
import math
import os
import time
from pathlib import Path
from mathutils import Vector
from bpy_extras.object_utils import world_to_camera_view

ROOT = Path(__file__).resolve().parent
SAMPLES = 64
ARRANGEMENTS = {
    '01-pedestal': 'Owner head resting on a single full bookshelf block as a library pedestal.',
    '02-bookend': 'Owner head beside a full bookshelf as an oversized character bookend.',
    '03-floating-portrait': 'Owner head floating in front of a two-by-two wall of full bookshelf blocks.',
    '04-collected-heads': 'Three identical owner heads lined up like collected editions atop two bookshelf blocks.',
    '05-bookshelf-hat': 'Owner head wearing a smaller full bookshelf block as a tall library hat.',
    '06-library-mosaic': 'Owner head replacing the upper-right block of a two-by-two bookshelf mosaic.',
}

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def mesh_signature(obj):
    data = {'vertices':[list(v.co) for v in obj.data.vertices], 'faces':[list(p.vertices) for p in obj.data.polygons], 'uv':[[list(d.uv) for d in layer.data] for layer in obj.data.uv_layers]}
    return hashlib.sha256(json.dumps(data,sort_keys=True).encode()).hexdigest()

def bounds(objects):
    pts = [world_to_camera_view(bpy.context.scene,bpy.context.scene.camera,o.matrix_world@v.co) for o in objects for v in o.data.vertices]
    return [min(p.x for p in pts),min(p.y for p in pts),max(p.x for p in pts),max(p.y for p in pts)]

def material(name, path):
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    bsdf = nodes.get('Principled BSDF')
    bsdf.inputs['Roughness'].default_value = .88
    bsdf.inputs['Specular IOR Level'].default_value = .15
    image = bpy.data.images.load(str(path),check_existing=True)
    image.pack()
    texture = nodes.new('ShaderNodeTexImage')
    texture.image = image
    texture.interpolation = 'Closest'
    mat.node_tree.links.new(texture.outputs['Color'],bsdf.inputs['Base Color'])
    return mat

def bookshelf(location, size):
    # Unmodified vanilla cube_column geometry: all six complete 16x16 faces, no bevel/recess.
    s = size/2
    vertices = [(-s,-s,-s),(s,-s,-s),(s,s,-s),(-s,s,-s),(-s,-s,s),(s,-s,s),(s,s,s),(-s,s,s)]
    faces = [(0,3,2,1),(4,5,6,7),(0,1,5,4),(1,2,6,5),(2,3,7,6),(3,0,4,7)]
    mesh = bpy.data.meshes.new('Vanilla bookshelf cube mesh')
    mesh.from_pydata(vertices,[],faces)
    mesh.update()
    obj = bpy.data.objects.new('Bookshelf %02d' % (len(SHELVES)+1),mesh)
    bpy.context.collection.objects.link(obj)
    obj.location = location
    obj['role'] = 'vanilla_bookshelf'
    obj['uniform_cube_side'] = size
    obj['model'] = 'minecraft:block/bookshelf -> cube_column -> cube, [0,0,0]..[16,16,16]'
    obj.data.materials.append(SIDE)
    obj.data.materials.append(END)
    uv = mesh.uv_layers.new(name='Vanilla full-face UV')
    for poly in mesh.polygons:
        poly.material_index = 1 if poly.index < 2 else 0
        for loop, coord in zip(poly.loop_indices,[(0,0),(1,0),(1,1),(0,1)]):
            uv.data[loop].uv = coord
    SHELVES.append(obj)
    return obj

def head(location, scale):
    root = bpy.data.objects.new('Owner head %02d' % (len(HEADS)+1),None)
    bpy.context.collection.objects.link(root)
    root.location = location
    root.rotation_euler = (0,0,math.pi/2)
    root.scale = (scale,scale,scale)
    root['role'] = 'owner_head'
    layers = []
    for template in TEMPLATES:
        obj = template.copy()
        obj.data = template.data.copy()
        obj.name = root.name+' / '+template.name
        bpy.context.collection.objects.link(obj)
        obj.parent = root
        obj.location = (0,0,0)
        obj.rotation_euler = (0,0,0)
        obj.scale = (1,1,1)
        obj['source_mesh_sha256'] = mesh_signature(template)
        assert mesh_signature(obj) == mesh_signature(template)
        layers.append(obj)
    HEADS.append((root,layers))
    return root

def arrange(kind):
    if kind == '01-pedestal':
        bookshelf((0,0,.60),1.20)
        head((0,0,1.20+.5625*.88),.88)
    elif kind == '02-bookend':
        bookshelf((-.08,-.62,.56),1.12)
        head((.16,.60,.5625*.95),.95)
    elif kind == '03-floating-portrait':
        for y in [-.5,.5]:
            for z in [.5,1.5]:
                bookshelf((-.62,y,z),1)
        head((1,0,1.10),1.05)
    elif kind == '04-collected-heads':
        for y in [-.5,.5]:
            bookshelf((0,y,.5),1)
        for y in [-.68,0,.68]:
            head((.02,y,1+.5625*.50),.50)
    elif kind == '05-bookshelf-hat':
        head((0,0,.5625*1.10),1.10)
        bookshelf((0,0,1.125*1.10+1.05/2),1.05)
    elif kind == '06-library-mosaic':
        for y,z in [(-.5625,.5625),(.5625,.5625),(-.5625,1.6875)]:
            bookshelf((0,y,z),1.125)
        head((0,.5625,1.6875),1)
    else:
        raise ValueError(kind)

def frame_camera():
    scene = bpy.context.scene
    camera = scene.camera
    camera.data.type = 'ORTHO'
    rotation = camera.rotation_euler.to_matrix()
    points = [o.matrix_world@v.co for o in SUBJECTS for v in o.data.vertices]
    local = [rotation.transposed()@p for p in points]
    lo = Vector([min(p[i] for p in local) for i in range(3)])
    hi = Vector([max(p[i] for p in local) for i in range(3)])
    center = rotation@((lo+hi)/2)
    camera.location = center+rotation@Vector((0,0,12))
    camera.data.ortho_scale = max(hi.x-lo.x,hi.y-lo.y)/.81
    camera.data.lens = 50
    camera.data.clip_end = 200
    return center

def setup_floor_and_lights(center):
    scene = bpy.context.scene
    # Seamless neutral sweep: one large matte plane, no visible horizon.
    bpy.ops.mesh.primitive_plane_add(size=200,location=(0,0,-.008))
    floor = bpy.context.object
    floor.name = 'Neutral matte contact-shadow floor'
    floor['role'] = 'backdrop'
    mat = bpy.data.materials.new('Neutral gray matte')
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get('Principled BSDF')
    bsdf.inputs['Base Color'].default_value = (.19,.19,.19,1)
    bsdf.inputs['Roughness'].default_value = 1
    bsdf.inputs['Specular IOR Level'].default_value = 0
    floor.data.materials.append(mat)
    # Reuse original lights/materials/color management to keep the reference head's palette.
    for obj in scene.objects:
        if obj.type == 'LIGHT':
            obj.location += center
            obj.rotation_euler = (center-obj.location).to_track_quat('-Z','Y').to_euler()
            if obj.data.type == 'AREA':
                obj.data.size = max(obj.data.size,4)
    scene.render.film_transparent = False
    return floor

def verify_scene():
    scene = bpy.context.scene
    camera = scene.camera
    bpy.context.view_layer.update()
    subject_bounds = bounds(SUBJECTS)
    assert min(subject_bounds) > .055 and max(subject_bounds) < .945,subject_bounds
    eye_dir = camera.rotation_euler.to_matrix()@Vector((0,0,1))
    head_rows = []
    for parent,layers in HEADS:
        assert len(layers)==2
        assert max(parent.scale)-min(parent.scale)<1e-7
        local_dir = parent.rotation_euler.to_matrix().transposed()@eye_dir
        yaw = math.degrees(math.atan2(local_dir.x,-local_dir.y))
        pitch = math.degrees(math.atan2(local_dir.z,math.hypot(local_dir.x,local_dir.y)))
        assert abs(yaw+45)<1e-4 and abs(pitch-35.264389682754654)<1e-4
        records=[]
        for obj,side in zip(layers,[1,1.125]):
            dims = [max(v.co[i] for v in obj.data.vertices)-min(v.co[i] for v in obj.data.vertices) for i in range(3)]
            assert all(abs(d-side)<1e-7 for d in dims)
            assert len(obj.data.polygons)==6 and len(obj.data.vertices)==24
            assert mesh_signature(obj)==obj['source_mesh_sha256']
            assert not obj.modifiers
            records.append({'name':obj.name,'layer':obj.get('skin_layer'),'dimensions_unscaled':dims,'mesh_and_uv_sha256':mesh_signature(obj),'identical_to_reference':True})
        front = [parent.matrix_world@Vector(v) for v in [(-.5,-.564,-.5),(.5,-.564,-.5),(.5,-.564,.5),(-.5,-.564,.5)]]
        projected = [world_to_camera_view(scene,camera,v) for v in front]
        assert projected[1].x>projected[0].x and projected[1].y>projected[0].y
        # Visibility ray test, all arrangements: front-center and quarter points must face camera unobstructed.
        visible=0
        for u in [-.3,0,.3]:
            for v in [-.3,0,.3]:
                point=parent.matrix_world@Vector((u,-.565,v))
                result=scene.ray_cast(bpy.context.evaluated_depsgraph_get(),point+eye_dir*10,-eye_dir,distance=12)
                if result[0] and result[4].name in [o.name for o in layers]:
                    visible+=1
        assert visible>=6,(parent.name,visible)
        bb=bounds(layers)
        assert (bb[2]-bb[0])*1024>120
        head_rows.append({'name':parent.name,'location':list(parent.location),'uniform_scale':parent.scale.x,'nmsr_yaw':-yaw,'pitch':pitch,'visible_front_probes':visible,'front_probe_count':9,'screen_bounds':bb,'layers':records,'front_polygon_pixels':[[p.x*1024,(1-p.y)*1024] for p in projected]})
    shelf_rows=[]
    for obj in SHELVES:
        dims=list(obj.dimensions)
        assert max(dims)-min(dims)<1e-6
        assert len(obj.data.vertices)==8 and len(obj.data.polygons)==6 and not obj.modifiers
        assert sum(p.material_index==0 for p in obj.data.polygons)==4
        assert sum(p.material_index==1 for p in obj.data.polygons)==2
        for p in obj.data.polygons:
            assert {tuple(obj.data.uv_layers.active.data[i].uv) for i in p.loop_indices} == {(0,0),(1,0),(1,1),(0,1)}
        shelf_rows.append({'name':obj.name,'location':list(obj.location),'cube_side':dims[0],'full_cube':True,'bookshelf_sides':4,'oak_plank_ends':2,'full_face_uvs':True,'screen_bounds':bounds([obj])})
    # Cube bounding boxes may touch but cannot intersect by positive volume.
    solid=[]
    for obj in SHELVES+[layers[1] for parent,layers in HEADS]:
        pts=[obj.matrix_world@Vector(p) for p in obj.bound_box]
        solid.append((obj.name,[min(p[i] for p in pts) for i in range(3)],[max(p[i] for p in pts) for i in range(3)]))
    for i,(a,lo,hi) in enumerate(solid):
        for b,lo2,hi2 in solid[i+1:]:
            overlap=[min(hi[k],hi2[k])-max(lo[k],lo2[k]) for k in range(3)]
            assert min(overlap)<1e-5,(a,b,overlap)
    return {'subject_screen_bounds':subject_bounds,'heads':head_rows,'bookshelves':shelf_rows,'positive_volume_intersections':0,'background':'neutral gray seamless matte floor with Cycles area-light contact shadows','nmsr_visible_faces':['front on screen-right','player-right side on screen-left','top']}

def render(kind):
    global TEMPLATES,HEADS,SHELVES,SIDE,END,SUBJECTS
    assert bpy.app.background and not os.environ.get('DISPLAY') and not os.environ.get('WAYLAND_DISPLAY')
    bpy.ops.wm.open_mainfile(filepath=str(ROOT/'assets/reference-head.blend'))
    scene=bpy.context.scene
    TEMPLATES=[bpy.data.objects['Base head'],bpy.data.objects['Outer hat layer']]
    # Preserve source mesh and UV data, removing only scene objects unrelated to this task.
    keep={scene.camera.name}|{o.name for o in scene.objects if o.type=='LIGHT'}
    for obj in list(scene.objects):
        if obj in TEMPLATES:
            for collection in list(obj.users_collection):
                collection.objects.unlink(obj)
        elif obj.name not in keep:
            bpy.data.objects.remove(obj,do_unlink=True)
    HEADS=[]
    SHELVES=[]
    SIDE=material('Unmodified vanilla bookshelf side',ROOT/'assets/bookshelf.png')
    END=material('Unmodified vanilla oak plank ends',ROOT/'assets/oak_planks.png')
    arrange(kind)
    # Detached source templates are not part of the render or saved scene.
    for obj in TEMPLATES:
        bpy.data.objects.remove(obj,do_unlink=True)
    SUBJECTS=SHELVES+[obj for parent,layers in HEADS for obj in layers]
    bpy.context.view_layer.update()
    center=frame_camera()
    setup_floor_and_lights(center)
    scene.render.engine='CYCLES'
    scene.cycles.device='CPU'
    scene.cycles.samples=SAMPLES
    scene.cycles.use_denoising=True
    scene.cycles.denoiser='OPENIMAGEDENOISE'
    scene.cycles.seed=11011
    scene.render.threads_mode='FIXED'
    scene.render.threads=2
    scene.render.resolution_x=scene.render.resolution_y=1024
    scene.render.resolution_percentage=100
    scene.render.image_settings.file_format='PNG'
    scene.render.image_settings.color_mode='RGBA'
    scene.render.image_settings.color_depth='8'
    stem='hudrendererlib-'+kind
    scene.render.filepath=str(ROOT/(stem+'.png'))
    bpy.context.preferences.filepaths.save_version=0
    verification=verify_scene()
    skin=[im for im in bpy.data.images if im.packed_file and im.size[0]==64]
    assert len(skin)==1
    assert hashlib.sha256(bytes(skin[0].packed_file.data)).hexdigest()==sha(ROOT/'assets/supplied-skin.png')
    for text in list(bpy.data.texts):
        bpy.data.texts.remove(text)
    for filename in ['scene.py',stem+'.py']:
        text=bpy.data.texts.load(str(ROOT/filename))
        text.use_fake_user=True
    bpy.ops.file.pack_all()
    images=[im for im in bpy.data.images if im.source=='FILE']
    assert all(im.packed_file for im in images)
    details={'project':'HudRendererLib','name':stem,'arrangement':ARRANGEMENTS[kind],'geometry':verification,'reference':'blender-h/npc-addons-head-2.png; exact base and outer head mesh/UV data, without NPCAddons plus','source_blend_sha256':sha(ROOT/'assets/reference-head.blend'),'skin_sha256':sha(ROOT/'assets/supplied-skin.png'),'script':stem+'.py','author_script_sha256':sha(ROOT/'scene.py'),'blender_version':bpy.app.version_string,'engine':scene.render.engine,'device':scene.cycles.device,'samples':SAMPLES,'resolution':[1024,1024],'packed_textures':[{'name':im.name,'bytes':len(im.packed_file.data),'sha256':hashlib.sha256(bytes(im.packed_file.data)).hexdigest()} for im in images],'camera':{'location':list(scene.camera.location),'rotation':list(scene.camera.rotation_euler),'ortho_scale':scene.camera.data.ortho_scale},'resources':{'background':True,'noaudio':True,'threads':2,'display':None,'audio':None,'gpu':None,'cpu_weight':20,'quota_cores':3,'cpu_affinity':sorted(os.sched_getaffinity(0)),'cgroup':Path('/proc/self/cgroup').read_text().strip()}}
    scene['hudrendererlib_metadata']=json.dumps(details)
    bpy.ops.wm.save_as_mainfile(filepath=str(ROOT/(stem+'.blend')))
    start=time.perf_counter()
    bpy.ops.render.render(write_still=True)
    details['render_duration_seconds']=time.perf_counter()-start
    details['png_sha256']=sha(ROOT/(stem+'.png'))
    decoded=bpy.data.images.load(str(ROOT/(stem+'.png')),check_existing=False)
    assert list(decoded.size)==[1024,1024]
    bpy.data.images.remove(decoded)
    (ROOT/(stem+'-metadata.json')).write_text(json.dumps(details,indent=2)+'\n')
    print('RENDER_COMPLETE '+json.dumps({'name':stem,'duration_seconds':details['render_duration_seconds'],'blender':bpy.app.version_string,'samples':SAMPLES,'engine':'CYCLES','device':'CPU'}),flush=True)
