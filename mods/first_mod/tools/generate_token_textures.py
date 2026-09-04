from pathlib import Path

from PIL import Image, ImageDraw


OUT_DIR = Path(__file__).resolve().parents[1] / "src/main/resources/assets/first_mod/textures/item"


def save(name, draw_fn):
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw_fn(draw)
    image.save(OUT_DIR / f"{name}.png")


def broken_exam_paper(draw):
    draw.rectangle((2, 1, 13, 14), fill=(235, 228, 196, 255), outline=(123, 105, 75, 255))
    draw.line((4, 4, 11, 4), fill=(70, 82, 95, 255))
    draw.line((4, 6, 10, 6), fill=(70, 82, 95, 255))
    draw.text((9, 8), "100", fill=(190, 40, 40, 255))
    draw.line((9, 2, 7, 5, 10, 8, 8, 12), fill=(60, 55, 50, 255))
    draw.polygon([(10, 13), (14, 11), (13, 14)], fill=(177, 168, 135, 255))


def broken_wristband(draw):
    draw.rectangle((3, 5, 6, 11), fill=(38, 78, 135, 255), outline=(19, 35, 73, 255))
    draw.rectangle((9, 5, 12, 11), fill=(38, 78, 135, 255), outline=(19, 35, 73, 255))
    draw.rectangle((6, 4, 9, 6), fill=(226, 185, 65, 255))
    draw.line((7, 6, 8, 8, 7, 10), fill=(232, 235, 240, 255))
    draw.line((8, 6, 7, 8, 8, 10), fill=(232, 235, 240, 255))


def burnt_note(draw):
    draw.rectangle((3, 2, 12, 13), fill=(202, 167, 105, 255), outline=(46, 31, 20, 255))
    draw.rectangle((2, 3, 3, 6), fill=(31, 22, 18, 255))
    draw.rectangle((12, 9, 13, 12), fill=(31, 22, 18, 255))
    draw.line((5, 5, 10, 5), fill=(76, 48, 34, 255))
    draw.line((5, 8, 9, 8), fill=(76, 48, 34, 255))
    draw.line((7, 11, 11, 7), fill=(120, 47, 34, 255))


def chipped_attendance_tag(draw):
    draw.rectangle((4, 2, 11, 13), fill=(84, 106, 118, 255), outline=(31, 45, 54, 255))
    draw.rectangle((5, 4, 10, 6), fill=(218, 223, 218, 255))
    draw.line((6, 9, 10, 9), fill=(218, 223, 218, 255))
    draw.rectangle((7, 1, 8, 2), fill=(218, 190, 82, 255))
    draw.polygon([(10, 2), (12, 2), (12, 5)], fill=(0, 0, 0, 0))


def cracked_phone_charm(draw):
    draw.rectangle((5, 4, 10, 12), fill=(212, 86, 130, 255), outline=(94, 34, 76, 255))
    draw.rectangle((6, 5, 9, 9), fill=(245, 175, 205, 255))
    draw.line((8, 4, 7, 7, 9, 9, 8, 12), fill=(37, 28, 48, 255))
    draw.rectangle((7, 1, 8, 4), fill=(230, 206, 104, 255))
    draw.point((6, 11), fill=(255, 232, 245, 255))


def crumpled_witness_note(draw):
    draw.polygon([(3, 3), (12, 2), (13, 12), (5, 14), (2, 9)], fill=(219, 211, 177, 255), outline=(104, 92, 68, 255))
    draw.line((4, 5, 11, 12), fill=(145, 134, 105, 255))
    draw.line((11, 3, 5, 13), fill=(145, 134, 105, 255))
    draw.line((5, 7, 10, 7), fill=(71, 81, 90, 255))
    draw.line((6, 10, 9, 10), fill=(71, 81, 90, 255))


def stained_paintbrush(draw):
    draw.line((7, 13, 9, 3), fill=(91, 54, 31, 255), width=2)
    draw.line((8, 13, 10, 3), fill=(184, 132, 69, 255), width=1)
    draw.rectangle((5, 2, 9, 5), fill=(74, 78, 86, 255), outline=(30, 34, 42, 255))
    draw.polygon([(5, 1), (10, 1), (9, 4), (6, 4)], fill=(54, 144, 112, 255))
    draw.point((4, 2), fill=(220, 64, 92, 255))
    draw.point((10, 3), fill=(220, 64, 92, 255))


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    save("broken_exam_paper", broken_exam_paper)
    save("broken_wristband", broken_wristband)
    save("burnt_note", burnt_note)
    save("chipped_attendance_tag", chipped_attendance_tag)
    save("cracked_phone_charm", cracked_phone_charm)
    save("crumpled_witness_note", crumpled_witness_note)
    save("stained_paintbrush", stained_paintbrush)


if __name__ == "__main__":
    main()
