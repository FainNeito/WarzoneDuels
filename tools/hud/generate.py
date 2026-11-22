"""Generate ZoneTitles-style duel HUD frames. Run from any directory with Python and Pillow."""
import json

from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2] / "resource-pack"
ASSETS = ROOT / "assets/warzoneduels"
ART = Path(__file__).resolve().parent / "art"
WIDTH, HEIGHT = 240, 48
icons = {name: Image.open(ART / (name + ".png")).convert("RGBA")
         for name in ("combat", "war_victory", "war_defeat")}

def load_masks(name):
    table = json.loads((ART/(name+".json")).read_text())
    masks = {}
    for c, rows in table.items():
        mask = Image.new("L",(len(rows[0]),len(rows)))
        mask.putdata([255 if bit=="#" else 0 for row in rows for bit in row])
        masks[c]=mask
    masks[" "]=Image.new("L",(2,5))
    return masks

name_masks=load_masks("NameGlyphs")
tag_masks=load_masks("SmallGlyphs")

def text_width(label, masks, scale):
    return (sum(masks[c].width+1 for c in label)-1)*scale

def stamp(im,mask,x,y,scale,color):
    tile=mask.resize((mask.width*scale,mask.height*scale),Image.Resampling.NEAREST)
    im.paste(color,(x,y,x+tile.width,y+tile.height),tile)


def render(word,mode,frame):
    im=Image.new("RGBA",(WIDTH,HEIGHT))
    draw=ImageDraw.Draw(im)
    accent,tag={"count":("#ff8a48","DUEL COUNTDOWN"),"fight":("#ff702d","COMBAT LIVE"),
                "victory":("#ffc84c","DUEL RESULT"),"defeat":("#ed8176","DUEL RESULT")}[mode]
    cx=WIDTH//2
    tx=cx-text_width(tag,tag_masks,1)//2
    for c in tag:
        stamp(im,tag_masks[c],tx,5,1,accent)
        tx+=tag_masks[c].width+1
    for sign in (-1,1):
        point=cx+sign*(text_width(tag,tag_masks,1)//2+6)
        draw.line((point,7,point+sign*12,7),fill=accent)
        draw.point((point+sign*(2+frame),7),fill="#fff1cb")
    if mode!="count":
        icon=icons[{"fight":"combat","defeat":"war_defeat","victory":"war_victory"}[mode]]
        im.alpha_composite(icon,(cx-text_width(tag,tag_masks,1)//2-36,0))
        im.alpha_composite(icon.transpose(Image.Transpose.FLIP_LEFT_RIGHT),(cx+text_width(tag,tag_masks,1)//2+20,0))
    x=cx-text_width(word,name_masks,4)//2
    visible=len(word) if mode in ("count","fight") else min(frame+1,len(word))
    for i,c in enumerate(word[:visible]):
        mask=name_masks[c]
        if mode in ("defeat","victory") and i==frame:
            # Displace a single scanline on the active letter. It settles on the next tick pair.
            flicker=mask.copy()
            y=(i*3+1)%6
            flicker.paste(mask.crop((0,y,mask.width-1,y+1)),(1,y))
            mask=flicker
        color=accent if (mode in ("defeat","victory") and i==frame or mode=="count" and frame==0 or mode=="fight" and i==frame) else "#FFF9EE"
        stamp(im,mask,x+2,17,4,"#342731")
        stamp(im,mask,x,15,4,color)
        x+=(name_masks[c].width+1)*4
    return im

def main():
    tex=ASSETS/"textures/font"
    tex.mkdir(parents=True,exist_ok=True)
    (ASSETS/"font").mkdir(parents=True,exist_ok=True)
    providers=[]
    for row,name in enumerate([f"countdown_{n}" for n in range(1,6)]+["fight","defeat","victory"]):
        mode = "count" if name.startswith("countdown") else name
        word = name[-1] if mode == "count" else name.upper()
        frames = [render(word, mode, f) for f in range(8)]
        margin=min(min(frame.getbbox()[0], WIDTH-frame.getbbox()[2]) for frame in frames)
        width=WIDTH-2*margin
        atlas=Image.new("RGBA",(width*8,HEIGHT))
        for f in range(8):
            cell=frames[f].crop((margin,0,WIDTH-margin,HEIGHT))
            # The bitmap provider trims trailing transparent columns when measuring advance.
            cell.putpixel((width-1,HEIGHT-1),(255,255,255,1))
            atlas.paste(cell,(width*f,0))
        atlas.save(tex/(name+".png"),optimize=True)
        providers.append(dict(type="bitmap",file="warzoneduels:font/"+name+".png",height=16,ascent=14,
                              chars=["".join(chr(0xE100+row*8+f) for f in range(8))]))
    (ASSETS/"font/duel_hud.json").write_text(json.dumps({"providers":providers},indent=2,ensure_ascii=False)+"\n",encoding="utf8")
    (ROOT/"pack.mcmeta").write_text(json.dumps({"pack":{"description":"WarzoneDuels animated arena titles","min_format":[88,0],"max_format":[88,0]}},indent=2)+"\n")

if __name__=="__main__":
    main()
