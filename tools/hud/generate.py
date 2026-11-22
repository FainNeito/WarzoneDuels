"""Generate original pixel HUD frames. Run from any directory with Python and Pillow."""
import json

from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2] / "resource-pack"
ASSETS = ROOT / "assets/warzoneduels"
W, H = 128, 48
FONT = {
    "1":"00100 01100 00100 00100 00100 00100 01110", "2":"01110 10001 00001 00110 01000 10000 11111",
    "3":"11110 00001 00001 01110 00001 00001 11110", "4":"00010 00110 01010 10010 11111 00010 00010",
    "5":"11111 10000 10000 11110 00001 00001 11110", "A":"01110 11011 11011 11111 11011 11011 11011",
    "C":"01111 11000 11000 11000 11000 11000 01111", "D":"11110 11011 11011 11011 11011 11011 11110",
    "E":"11111 11000 11000 11110 11000 11000 11111", "F":"11111 11000 11000 11110 11000 11000 11000",
    "G":"01111 11000 11000 11011 11011 11011 01111", "H":"11011 11011 11011 11111 11011 11011 11011",
    "I":"11111 00100 00100 00100 00100 00100 11111", "O":"01110 11011 11011 11011 11011 11011 01110",
    "R":"11110 11011 11011 11110 11100 11010 11011", "T":"11111 00100 00100 00100 00100 00100 00100",
    "V":"11011 11011 11011 11011 11011 01010 00100", "Y":"11011 11011 01010 00100 00100 00100 00100",
    "!":"00100 01110 01110 00100 00100 00000 00100",
}
PALETTES = {
    "countdown": ("#fff2b6","#ffbf45","#e3801e","#73371d"), "fight": ("#f2ffd1","#b1ef40","#69af27","#34551c"),
    "defeat": ("#ffe5ce","#f89479","#d9514f","#682b3c"), "victory": ("#fff8ca","#ffdb66","#efa338","#88511c"),
}

def lettering(image, word, top, palette, scale, frame):
    mask = Image.new("L", image.size)
    draw = ImageDraw.Draw(mask)
    left = (W - (len(word) * 6 - 1) * scale) // 2
    for index, letter in enumerate(word):
        for y, row in enumerate(FONT[letter].split()):
            for x, bit in enumerate(row):
                if bit == "1":
                    px, py = left + (6 * index + x) * scale, top + y * scale
                    draw.rectangle((px, py, px+scale-1, py+scale-1), fill=255)
    outline = mask.filter(ImageFilter.MaxFilter(3))
    image.paste("#291e2c", (0, 2), outline)
    image.paste(palette[3], (0, 1), outline)
    image.paste("#35252b", (0, 0), outline)
    fill = Image.new("RGBA", image.size, palette[1])
    ink = ImageDraw.Draw(fill)
    ink.rectangle((0, top+4*scale, W, H), fill=palette[2])
    ink.line((0, top, W, top), fill=palette[0])
    shine = int(W*(frame-1)/6)
    ink.polygon([(shine,top),(shine+3,top),(shine-4,top+7*scale),(shine-7,top+7*scale)],fill=palette[0])
    image.paste(fill,(0,0),mask)

def spark(d,x,y,color):
    d.line((x-1,y,x+1,y),fill=color)
    d.line((x,y-1,x,y+1),fill=color)

def render(name, frame):
    im = Image.new("RGBA",(W,H))
    d = ImageDraw.Draw(im)
    kind = "countdown" if name.startswith("countdown") else name
    p = PALETTES[kind]
    if kind == "countdown":
        number = int(name[-1])
        scale = 4 if frame < 2 else 3
        lettering(im,str(number),6+(4-scale)*4,p,scale,frame)
        gap = [4,3,2,1,0,0,0,0][frame]
        for sign in (-1,1):
            x = 64 + sign*(25+gap)
            d.line((x,17,x-sign*3,20,x,23),fill=p[1],width=2)
        for i in range(5):
            x = 48+i*8
            d.rectangle((x-2,39,x+2,41),fill=p[1] if i<number else p[3])
        spark(d,82+frame,9+frame%3,p[0])
    elif kind == "fight":
        lettering(im,"FIGHT!",22-(1 if frame==1 else 0),p,2,frame)
        for sign in (-1,1):
            shift = max(0,5-frame*2)
            x = 64+sign*shift
            d.line((x-sign*6,17,x+sign*7,4),fill="#292531",width=4)
            d.line((x-sign*4,15,x+sign*7,4),fill="#f6f5e5",width=2)
            d.line((x-sign*7,13,x-sign*2,18),fill=p[1],width=2)
            d.line((64+sign*42,28,64+sign*(46+frame),28),fill=p[1],width=2)
    elif kind == "defeat":
        lettering(im,"DEFEAT",23,p,2,frame)
        for sign in (-1,1):
            x = 64 + sign*(1+frame//2)
            y = 4 + (frame//3 if sign==1 else 0)
            d.polygon([(x,y),(x+sign*7,y+1),(x+sign*7,y+7),(x+sign*3,y+12),(x,y+14),
                       (x+sign*2,y+8),(x,y+6)],fill=p[2],outline=p[3])
            d.line((x+sign*2,y+2,x+sign*5,y+2),fill=p[0])
            d.point((x+sign*(11+frame),12+frame), fill=p[1])
    else:
        lettering(im,"VICTORY",24,p,2,frame)
        for sign in (-1,1):
            for leaf in range(min(5,frame+1)):
                x,y=64+sign*(4+leaf*2),17-leaf*2
                d.line((x-sign*3,y+1,x,y),fill=p[2])
                d.polygon([(x,y),(x+sign*2,y-4),(x+sign*3,y-1)],fill=p[1])
            spark(d,64+sign*(19+frame),12-(frame%4),p[0])
        d.polygon([(58,7),(61,10),(64,5),(67,10),(70,7),(68,14),(60,14)],fill=p[1],outline=p[3])
    return im

def main():
    tex=ASSETS/"textures/font"
    tex.mkdir(parents=True,exist_ok=True)
    (ASSETS/"font").mkdir(parents=True,exist_ok=True)
    providers=[]
    for row,name in enumerate([f"countdown_{n}" for n in range(1,6)]+["fight","defeat","victory"]):
        frames=[render(name,f) for f in range(8)]
        margin=min(min(frame.getbbox()[0], W-frame.getbbox()[2]) for frame in frames)
        width=W-2*margin
        atlas=Image.new("RGBA",(width*8,H))
        for f in range(8):
            cell=frames[f].crop((margin,0,W-margin,H))
            # The bitmap provider trims trailing transparent columns when measuring advance.
            cell.putpixel((width-1,H-1),(255,255,255,1))
            atlas.paste(cell,(width*f,0))
        atlas.save(tex/(name+".png"),optimize=True)
        providers.append(dict(type="bitmap",file="warzoneduels:font/"+name+".png",height=16,ascent=14,
                              chars=["".join(chr(0xE100+row*8+f) for f in range(8))]))
    (ASSETS/"font/duel_hud.json").write_text(json.dumps({"providers":providers},indent=2,ensure_ascii=False)+"\n",encoding="utf8")
    (ROOT/"pack.mcmeta").write_text(json.dumps({"pack":{"description":"WarzoneDuels animated arena titles","min_format":[88,0],"max_format":[88,0]}},indent=2)+"\n")

if __name__=="__main__":
    main()
