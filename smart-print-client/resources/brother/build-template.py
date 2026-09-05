"""生成只有一个受控图像对象的 LBX；正式启用前须由 P-touch Editor/实机验收。"""
from pathlib import Path
from struct import pack
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED

root = Path(__file__).resolve().parent
namespace = 'http://schemas.brother.info/ptouch/2007/lbx/'
for color, name in [(False, 'visitor-image.lbx'), (True, 'visitor-image-black-red.lbx')]:
    xml = f'''<?xml version="1.0" encoding="UTF-8"?>
<pt:document xmlns:pt="{namespace}main" xmlns:style="{namespace}style" xmlns:image="{namespace}image" version="1.7" generator="Smart Print Client">
<pt:body currentSheet="Page" direction="LTR"><style:sheet name="Page">
<style:paper media="0" width="175.748031pt" height="226.771654pt" marginLeft="5.669291pt" marginTop="5.669291pt" marginRight="5.669291pt" marginBottom="5.669291pt" orientation="portrait" autoLength="false" monochromeDisplay="true" printColorDisplay="{'true' if color else 'false'}" printColorsID="{'129' if color else '0'}" paperColor="#FFFFFF" paperInk="#000000" split="1" format="259" backgroundTheme="0" printerID="14388" printerName="Brother QL-800"/>
<style:cutLine regularCut="0pt" freeCut=""/><style:backGround x="5.669291pt" y="5.669291pt" width="164.409449pt" height="215.433071pt" brushStyle="NULL" brushId="0" userPattern="NONE" userPatternId="0" color="#FFFFFF" printColorNumber="0" backColor="#FFFFFF" backPrintColorNumber="0"/>
<pt:objects><image:image><pt:objectStyle x="5.669291pt" y="5.669291pt" width="164.409449pt" height="215.433071pt" backColor="#FFFFFF" backPrintColorNumber="0" ropMode="COPYPEN" angle="0" anchor="TOPLEFT" flip="NONE"><pt:pen style="NULL" widthX="0.5pt" widthY="0.5pt" color="#000000" printColorNumber="1"/><pt:brush style="NULL" color="#FFFFFF" printColorNumber="0" id="0"/><pt:expanded objectName="PageImage" ID="0" lock="0" templateMergeTarget="NONE" templateMergeType="NONE" templateMergeID="0" linkStatus="NONE" linkID="0"/></pt:objectStyle><image:imageStyle originalName="" alignInText="LEFT" firstMerge="true" fileName="Object0.bmp"><image:transparent flag="false" color="#FFFFFF"/><image:trimming flag="false" shape="RECTANGLE" trimOrgX="0pt" trimOrgY="0pt" trimOrgWidth="164.409449pt" trimOrgHeight="215.433071pt"/><image:orgPos x="5.669291pt" y="5.669291pt" width="164.409449pt" height="215.433071pt"/><image:effect effect="NONE" brightness="50" contrast="50" photoIndex="4"/><image:mono operationKind="BINARY" reverse="0" ditherKind="MESH" threshold="128" gamma="100" ditherEdge="0" rgbconvProportionRed="30" rgbconvProportionGreen="59" rgbconvProportionBlue="11" rgbconvProportionReversed="0"/></image:imageStyle></image:image></pt:objects></style:sheet></pt:body></pt:document>'''
    pixels = bytes([255]) * (8 * 8 * 3)
    bmp = b'BM' + pack('<IHHI', 54 + len(pixels), 0, 0, 54) + pack('<IiiHHIIiiII', 40, 8, 8, 1, 24, 0, len(pixels), 11811, 11811, 0, 0) + pixels
    props = f'<?xml version="1.0" encoding="UTF-8"?><meta:properties xmlns:meta="{namespace}meta"><meta:numPages>1</meta:numPages></meta:properties>'
    with ZipFile(root / name, 'w', compression=ZIP_DEFLATED) as archive:
        for filename, data in [('label.xml', xml.encode()), ('Object0.bmp', bmp), ('prop.xml', props.encode())]:
            info = ZipInfo(filename, (2026, 9, 5, 0, 0, 0)); info.compress_type = ZIP_DEFLATED
            archive.writestr(info, data)
