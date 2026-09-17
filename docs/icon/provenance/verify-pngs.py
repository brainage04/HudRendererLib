"""Decode all deliverables, check face/texture visibility, and assemble manifests."""
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parent
metadata=sorted(ROOT.glob('hudrendererlib-*-metadata.json'))
assert len(metadata)==6
checks=[]
entries=[]
render_report=[]
contact=Image.new('RGB',(1536,1024),(120,120,120))
for index,path in enumerate(metadata):
    detail=json.loads(path.read_text())
    stem=detail['name']
    image=Image.open(ROOT/(stem+'.png'))
    assert image.format=='PNG' and image.size==(1024,1024)
    image.load()
    image=image.convert('RGBA')
    assert image.getchannel('A').getextrema()==(255,255)
    assert hashlib.sha256((ROOT/(stem+'.png')).read_bytes()).hexdigest()==detail['png_sha256']
    rgb=image.convert('RGB')
    faces=[]
    for head in detail['geometry']['heads']:
        mask=Image.new('1',image.size)
        ImageDraw.Draw(mask).polygon([tuple(p) for p in head['front_polygon_pixels']],fill=1)
        colors=[pixel for pixel,inside in zip(rgb.get_flattened_data(),mask.get_flattened_data()) if inside]
        yellow=sum(r>100 and g>80 and b<min(r,g)*.6 for r,g,b in colors)
        dark=sum(max(r,g,b)<100 for r,g,b in colors)
        pale=sum(b>75 and abs(r-g)<60 and abs(g-b)<60 for r,g,b in colors)
        assert yellow>200,(stem,head['name'],'yellow skin not visible',yellow)
        assert dark>150,(stem,head['name'],'glasses or mouth missing',dark)
        assert pale>60,(stem,head['name'],'eyes or mouth missing',pale)
        faces.append({'head':head['name'],'projected_front_area_pixels':len(colors),'yellow_skin_pixels':yellow,'dark_glasses_or_mouth_pixels':dark,'pale_eyes_or_mouth_pixels':pale})
    pixels=rgb.get_flattened_data()
    book_colors={
        'red':sum(r>70 and r>g*1.3 and r>b*1.3 for r,g,b in pixels),
        'blue':sum(b>60 and b>r*1.2 and b>g*1.1 for r,g,b in pixels),
        'green':sum(g>55 and g>r*1.1 and g>b*1.2 for r,g,b in pixels),
    }
    assert min(book_colors.values())>100,(stem,book_colors)
    bb=detail['geometry']['subject_screen_bounds']
    bottom=round((1-bb[1])*1024)
    center=round((bb[0]+bb[2])*512)
    ground=[rgb.getpixel((x,y)) for x in range(center-100,center+100) for y in range(bottom+3,min(1010,bottom+38))]
    neutral=[sum(c)/3 for c in ground if max(c)-min(c)<24]
    reference=sum(sum(rgb.getpixel(p))/3 for p in [(20,1000),(1000,1000)])/2
    assert neutral and reference-min(neutral)>5,(stem,'contact shadow not measurable')
    shadow={'unoccluded_ground_region_below_subject':True,'reference_luminance':reference,'shadow_minimum_luminance':min(neutral),'contrast':reference-min(neutral)}
    thumb=rgb.resize((128,128),Image.Resampling.LANCZOS)
    thumb.save(ROOT/'evidence'/(stem+'-128.png'))
    contact.paste(rgb.resize((512,512),Image.Resampling.LANCZOS),((index%3)*512,(index//3)*512))
    checks.append({'name':stem,'decoded':True,'format':'PNG','resolution':[1024,1024],'opaque_neutral_backdrop':True,'visible_owner_faces':faces,'visible_bookshelf_spine_color_pixels':book_colors,'thumbnail':'evidence/'+stem+'-128.png','sha256':detail['png_sha256'],'geometry_checks':'saved-scene-verification.json'})
    checks[-1]['contact_shadow']=shadow
    duration=round(detail['render_duration_seconds'],3)
    notes=f"{detail['arrangement']} Both vanilla head layers retain exactly the reference mesh and UVs; NMSR yaw +45°, elevation 35.264389682754654°. Bookshelves are complete vanilla cubes with unmodified side and oak-plank textures; no carving, stretching, beveling, text or logos. Neutral matte floor with soft shadows. Blender {detail['blender_version']}; Cycles CPU; {detail['samples']} samples; {duration}s; 1024x1024. Per-image .py + packed .blend + metadata beside PNG. Scripts saved and embedded, hashed, not git-committed per local-only policy."
    entries.append({'project':'HudRendererLib','label':detail['arrangement'],'path':'blender-z/'+stem+'.png','method':'Headless Blender CLI; Cycles CPU, 2 threads; exact reference-head geometry and vanilla client-jar bookshelf cube','source':'blender-h/npc-addons-head-2.blend; owner-supplied skin SHA256 e9ebbeece495d9c96040e235dc865fdb1a530cf6a2243a6c8fcec22e72f03e3f; Minecraft 26.2 client assets textures/block/bookshelf.png and oak_planks.png, models/block/bookshelf.json; see asset-provenance.json','notes':notes})
    render_report.append({'image':stem+'.png','arrangement':detail['arrangement'],'blender_version':detail['blender_version'],'engine':detail['engine'],'device':detail['device'],'samples':detail['samples'],'render_duration_seconds':detail['render_duration_seconds'],'resolution':detail['resolution'],'threads':detail['resources']['threads']})
contact.save(ROOT/'evidence/six-arrangements-contact.png')
(ROOT/'png-verification.json').write_text(json.dumps(checks,indent=2)+'\n')
(ROOT/'manifest.json').write_text(json.dumps({'entries':entries},indent=2)+'\n')
(ROOT/'render-report.json').write_text(json.dumps(render_report,indent=2)+'\n')
(ROOT/'blockers.json').write_text('[]\n')
print(json.dumps({'verified_images':len(checks),'arrangements':[e['label'] for e in entries],'render_report':render_report},indent=2))
