#!/usr/bin/env python3
"""Render the pinned v8-chroma SVG masters; Pillow is a development dependency."""
from pathlib import Path
import argparse,io,sys
sys.path.insert(0,str(Path(__file__).resolve().parent/'vendor'))
from chroma_icon_raster import raster

ROOT=Path(__file__).resolve().parents[1]/'bundles/ru.litonchik.edt.themes/resources/icons/chroma'
SIZES=(16,20,24,28,32,48,64)
def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args();count=0
    for source in sorted((ROOT/'svg').glob('*/*.svg')):
        for size in SIZES:
            target=ROOT/'png'/source.parent.name/str(size)/(source.stem+'.png')
            buffer=io.BytesIO();raster(source.read_text(encoding='utf-8'),size).save(buffer,format='PNG');data=buffer.getvalue()
            if args.check:
                if not target.is_file() or target.read_bytes()!=data:raise SystemExit('Generated asset differs: '+str(target.relative_to(ROOT)))
            else:target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
            count+=1
    print(('Verified' if args.check else 'Generated')+f' {count} transparent PNGs from pinned SVG masters')
if __name__=='__main__':main()
