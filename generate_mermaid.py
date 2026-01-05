#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script tự động tạo mã Mermaid cho biểu đồ lớp (Class Diagram)
Thể hiện mối quan hệ giữa các lớp trong từng package
"""

import os
import re
from pathlib import Path
from typing import List, Dict, Set, Tuple
from dataclasses import dataclass, field
from collections import defaultdict

@dataclass
class ClassInfo:
    """Thông tin về một lớp"""
    name: str
    package: str
    type: str  # 'class', 'interface', 'enum'
    attributes: List[str] = field(default_factory=list)
    methods: List[str] = field(default_factory=list)
    imports: List[str] = field(default_factory=list)
    file_path: str = ""
    extends: str = ""  # Class/interface mà nó extends
    implements: List[str] = field(default_factory=list)  # Interfaces mà nó implements

class JavaParser:
    """Parser để đọc và phân tích file Java"""
    
    def __init__(self, base_path: str):
        self.base_path = Path(base_path)
        self.classes: Dict[str, ClassInfo] = {}
        
    def parse_file(self, file_path: Path) -> ClassInfo:
        """Parse một file Java và trả về ClassInfo"""
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Tìm package
        package_match = re.search(r'package\s+([\w.]+);', content)
        package = package_match.group(1) if package_match else ""
        
        # Tìm class/interface/enum
        class_match = re.search(r'public\s+(class|interface|enum)\s+(\w+)', content)
        if not class_match:
            return None
        
        class_type = class_match.group(1)
        class_name = class_match.group(2)
        
        class_info = ClassInfo(
            name=class_name,
            package=package,
            type=class_type,
            file_path=str(file_path.relative_to(self.base_path))
        )
        
        # Tìm extends
        extends_match = re.search(r'extends\s+(\w+)', content)
        if extends_match:
            class_info.extends = extends_match.group(1)
        
        # Tìm implements
        implements_match = re.search(r'implements\s+([\w,\s]+)', content)
        if implements_match:
            impls = [i.strip() for i in implements_match.group(1).split(',')]
            class_info.implements = impls
        
        # Tìm imports
        imports = re.findall(r'import\s+([\w.]+);', content)
        class_info.imports = [imp for imp in imports if 'com.bluemoon' in imp]
        
        # Tìm attributes (private fields)
        attr_pattern = r'private\s+(?:static\s+)?(?:final\s+)?(\w+(?:<.*?>)?)\s+(\w+)\s*[;=]'
        attributes = re.findall(attr_pattern, content)
        class_info.attributes = [f"{attr_type} {attr_name}" for attr_type, attr_name in attributes]
        
        return class_info
    
    def scan_project(self):
        """Quét toàn bộ project để tìm các file Java"""
        java_files = list(self.base_path.rglob('*.java'))
        
        for java_file in java_files:
            # Bỏ qua test files
            if 'test' in str(java_file).lower():
                continue
            
            class_info = self.parse_file(java_file)
            if class_info:
                full_name = f"{class_info.package}.{class_info.name}"
                self.classes[full_name] = class_info

class MermaidGenerator:
    """Generator để tạo mã Mermaid cho class diagram"""
    
    def __init__(self, parser: JavaParser):
        self.parser = parser
    
    def generate_all_diagrams(self, output_file: str = "Class_Diagrams.md"):
        """Tạo file markdown chứa tất cả các biểu đồ Mermaid"""
        output_path = Path(__file__).parent / output_file
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write("# BIỂU ĐỒ LỚP - HỆ THỐNG QUẢN LÝ CHUNG CƯ BLUEMOON\n")
            f.write("*(File này được tạo tự động bởi generate_mermaid.py)*\n\n")
            f.write("---\n\n")
            
            # Nhóm theo package
            packages = defaultdict(list)
            for class_info in self.parser.classes.values():
                packages[class_info.package].append(class_info)
            
            for package_name in sorted(packages.keys()):
                f.write(f"## Package: {package_name}\n\n")
                mermaid_code = self._generate_package_diagram(packages[package_name], package_name)
                f.write("```mermaid\n")
                f.write(mermaid_code)
                f.write("```\n\n")
                f.write("---\n\n")
    
    def _generate_package_diagram(self, classes: List[ClassInfo], package_name: str) -> str:
        """Tạo mã Mermaid cho một package"""
        lines = []
        lines.append("classDiagram")
        lines.append("")
        
        # Định nghĩa các lớp
        for class_info in sorted(classes, key=lambda x: x.name):
            class_def = self._generate_class_definition(class_info)
            lines.append(class_def)
        
        lines.append("")
        
        # Định nghĩa các quan hệ
        for class_info in sorted(classes, key=lambda x: x.name):
            relations = self._generate_relations(class_info, classes)
            if relations:
                lines.extend(relations)
        
        return "\n".join(lines)
    
    def _generate_class_definition(self, class_info: ClassInfo) -> str:
        """Tạo định nghĩa lớp trong Mermaid"""
        lines = []
        
        # Xác định loại lớp
        if class_info.type == 'interface':
            class_type = "<<interface>>"
        elif class_info.type == 'enum':
            class_type = "<<enumeration>>"
        else:
            class_type = ""
        
        # Tên lớp với type
        if class_type:
            lines.append(f"    class {class_info.name} {class_type} {{")
        else:
            lines.append(f"    class {class_info.name} {{")
        
        # Thêm một số thuộc tính quan trọng (giới hạn để diagram không quá dài)
        if class_info.attributes:
            for attr in class_info.attributes[:8]:  # Giới hạn 8 thuộc tính
                # Format: -attributeName : Type (Mermaid format)
                attr_parts = attr.split()
                if len(attr_parts) >= 2:
                    attr_type = attr_parts[0]
                    attr_name = attr_parts[1]
                    lines.append(f"        -{attr_name} : {attr_type}")
        
        lines.append("    }")
        
        return "\n".join(lines)
    
    def _generate_relations(self, class_info: ClassInfo, all_classes: List[ClassInfo]) -> List[str]:
        """Tạo các quan hệ cho một lớp"""
        relations = []
        
        # Quan hệ extends (inheritance)
        if class_info.extends:
            # Tìm class trong cùng package hoặc imports
            target = self._find_class_in_context(class_info.extends, all_classes, class_info.package)
            if target:
                relations.append(f"    {target} <|-- {class_info.name} : extends")
        
        # Quan hệ implements
        for impl in class_info.implements:
            target = self._find_class_in_context(impl, all_classes, class_info.package)
            if target:
                relations.append(f"    {target} <|.. {class_info.name} : implements")
        
        # Quan hệ association (từ attributes)
        for attr in class_info.attributes:
            attr_parts = attr.split()
            if len(attr_parts) >= 2:
                attr_type = attr_parts[0]
                # Kiểm tra xem có phải là class trong project không
                target = self._find_class_by_name(attr_type, all_classes)
                if target and target != class_info.name:
                    # Chỉ thêm nếu chưa có quan hệ extends/implements
                    if attr_type != class_info.extends and attr_type not in class_info.implements:
                        relations.append(f"    {class_info.name} --> {target} : uses")
        
        return relations
    
    def _find_class_in_context(self, class_name: str, all_classes: List[ClassInfo], current_package: str) -> str:
        """Tìm class trong context (cùng package hoặc imports)"""
        # Tìm trong cùng package trước
        for cls in all_classes:
            if cls.name == class_name and cls.package == current_package:
                return cls.name
        
        # Tìm trong toàn bộ project
        for full_name, cls in self.parser.classes.items():
            if cls.name == class_name:
                return cls.name
        
        return None
    
    def _find_class_by_name(self, class_name: str, all_classes: List[ClassInfo]) -> str:
        """Tìm class theo tên trong danh sách"""
        for cls in all_classes:
            if cls.name == class_name:
                return cls.name
        
        # Tìm trong toàn bộ project
        for full_name, cls in self.parser.classes.items():
            if cls.name == class_name:
                return cls.name
        
        return None
    
    def generate_combined_diagram(self, output_file: str = "Class_Diagram_Combined.md"):
        """Tạo biểu đồ tổng hợp cho toàn bộ hệ thống"""
        output_path = Path(__file__).parent / output_file
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write("# BIỂU ĐỒ LỚP TỔNG HỢP - HỆ THỐNG QUẢN LÝ CHUNG CƯ BLUEMOON\n")
            f.write("*(File này được tạo tự động bởi generate_mermaid.py)*\n\n")
            f.write("---\n\n")
            
            f.write("```mermaid\n")
            f.write("classDiagram\n")
            f.write("\n")
            
            # Nhóm theo package
            packages = defaultdict(list)
            for class_info in self.parser.classes.values():
                packages[class_info.package].append(class_info)
            
            # Định nghĩa tất cả các lớp
            for package_name in sorted(packages.keys()):
                f.write(f"    %% Package: {package_name}\n")
                for class_info in sorted(packages[package_name], key=lambda x: x.name):
                    class_def = self._generate_class_definition(class_info)
                    # Không thêm indent thêm vì _generate_class_definition đã có indent đúng
                    f.write(class_def)
                    f.write("\n")
                f.write("\n")
            
            # Định nghĩa các quan hệ
            f.write("    %% Relationships\n")
            for class_info in sorted(self.parser.classes.values(), key=lambda x: x.name):
                # Quan hệ extends
                if class_info.extends:
                    target = self._find_class_in_all(class_info.extends)
                    if target:
                        f.write(f"    {target} <|-- {class_info.name} : extends\n")
                
                # Quan hệ implements
                for impl in class_info.implements:
                    target = self._find_class_in_all(impl)
                    if target:
                        f.write(f"    {target} <|.. {class_info.name} : implements\n")
            
            # Quan hệ association chính (chỉ các quan hệ quan trọng)
            important_relations = self._get_important_relations()
            for source, target, label in important_relations:
                f.write(f"    {source} --> {target} : {label}\n")
            
            f.write("```\n")
    
    def _find_class_in_all(self, class_name: str) -> str:
        """Tìm class trong toàn bộ project"""
        for full_name, cls in self.parser.classes.items():
            if cls.name == class_name:
                return cls.name
        return None
    
    def _get_important_relations(self) -> List[Tuple[str, str, str]]:
        """Lấy các quan hệ quan trọng giữa các package"""
        relations = []
        
        # Models -> Services (dependency)
        for class_info in self.parser.classes.values():
            if 'models' in class_info.package:
                # Tìm service sử dụng model này
                for other in self.parser.classes.values():
                    if 'services' in other.package and class_info.name in str(other.imports):
                        relations.append((other.name, class_info.name, "uses"))
        
        # Services -> Services (dependency)
        for class_info in self.parser.classes.values():
            if 'services.impl' in class_info.package:
                # Tìm các service khác được sử dụng
                for other in self.parser.classes.values():
                    if other.name != class_info.name and other.name in str(class_info.imports):
                        if 'Service' in other.name:
                            relations.append((class_info.name, other.name, "depends"))
        
        return relations
    
    def generate_household_diagram(self, output_file: str = "Class_Diagram_HoGiaDinh.md"):
        """Tạo biểu đồ Mermaid riêng cho module Hộ gia đình"""
        output_path = Path(__file__).parent / output_file
        
        # Danh sách các lớp liên quan đến Hộ gia đình
        household_related_classes = [
            'HoGiaDinh', 'Phong', 'NhanKhau', 'PhieuThu', 
            'ChiTietThu', 'PhuongTien', 'LichSuNhanKhau',
            'HoGiaDinhService', 'HoGiaDinhServiceImpl',
            'NhanKhauService', 'NhanKhauServiceImpl',
            'PhieuThuService', 'PhieuThuServiceImpl'
        ]
        
        # Lọc các lớp liên quan
        related_classes = []
        for full_name, class_info in self.parser.classes.items():
            if class_info.name in household_related_classes:
                related_classes.append(class_info)
        
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write("# BIỂU ĐỒ LỚP - MODULE HỘ GIA ĐÌNH\n")
            f.write("*(File này được tạo tự động bởi generate_mermaid.py)*\n\n")
            f.write("---\n\n")
            f.write("## Mô tả\n\n")
            f.write("Biểu đồ này thể hiện các lớp và mối quan hệ trong module quản lý Hộ gia đình, bao gồm:\n")
            f.write("- **Models**: HoGiaDinh, Phong, NhanKhau, PhieuThu, ChiTietThu, PhuongTien, LichSuNhanKhau\n")
            f.write("- **Services**: HoGiaDinhService, NhanKhauService, PhieuThuService và các implementation\n\n")
            f.write("---\n\n")
            
            f.write("```mermaid\n")
            f.write("classDiagram\n")
            f.write("\n")
            
            # Định nghĩa các lớp Models
            f.write("    %% Models\n")
            model_classes = [c for c in related_classes if 'models' in c.package]
            for class_info in sorted(model_classes, key=lambda x: x.name):
                class_def = self._generate_class_definition(class_info)
                f.write(class_def)
                f.write("\n")
            
            f.write("\n")
            
            # Định nghĩa các lớp Services (Interfaces)
            f.write("    %% Service Interfaces\n")
            interface_classes = [c for c in related_classes if c.type == 'interface' and 'services' in c.package]
            for class_info in sorted(interface_classes, key=lambda x: x.name):
                class_def = self._generate_class_definition(class_info)
                f.write(class_def)
                f.write("\n")
            
            f.write("\n")
            
            # Định nghĩa các lớp Service Implementations
            f.write("    %% Service Implementations\n")
            impl_classes = [c for c in related_classes if 'services.impl' in c.package]
            for class_info in sorted(impl_classes, key=lambda x: x.name):
                class_def = self._generate_class_definition(class_info)
                f.write(class_def)
                f.write("\n")
            
            f.write("\n")
            
            # Định nghĩa các quan hệ
            f.write("    %% Relationships\n")
            
            # 1. Service Implementation -> Service Interface
            for class_info in impl_classes:
                for impl in class_info.implements:
                    target = self._find_class_in_all(impl)
                    if target:
                        f.write(f"    {target} <|.. {class_info.name} : implements\n")
            
            # 2. Phong -> HoGiaDinh (soPhong: một phòng có thể có nhiều hộ gia đình theo thời gian)
            if self._find_class_in_all('HoGiaDinh') and self._find_class_in_all('Phong'):
                f.write("    Phong ||--o{ HoGiaDinh : \"soPhong\"\n")
            
            # 3. NhanKhau -> HoGiaDinh (maChuHo = soCCCD: một nhân khẩu có thể là chủ hộ của một hộ)
            if self._find_class_in_all('HoGiaDinh') and self._find_class_in_all('NhanKhau'):
                f.write("    NhanKhau ||--o| HoGiaDinh : \"maChuHo (soCCCD)\"\n")
            
            # 4. HoGiaDinh -> NhanKhau (maHo: một hộ có nhiều nhân khẩu)
            if self._find_class_in_all('HoGiaDinh') and self._find_class_in_all('NhanKhau'):
                f.write("    HoGiaDinh ||--o{ NhanKhau : \"maHo\"\n")
            
            # 5. HoGiaDinh -> PhieuThu (maHo: một hộ có nhiều phiếu thu)
            if self._find_class_in_all('HoGiaDinh') and self._find_class_in_all('PhieuThu'):
                f.write("    HoGiaDinh ||--o{ PhieuThu : \"maHo\"\n")
            
            # 6. PhieuThu -> ChiTietThu (maPhieu: một phiếu thu có nhiều chi tiết)
            if self._find_class_in_all('PhieuThu') and self._find_class_in_all('ChiTietThu'):
                f.write("    PhieuThu ||--o{ ChiTietThu : \"maPhieu\"\n")
            
            # 7. HoGiaDinh -> PhuongTien (maHo: một hộ có nhiều phương tiện)
            if self._find_class_in_all('HoGiaDinh') and self._find_class_in_all('PhuongTien'):
                f.write("    HoGiaDinh ||--o{ PhuongTien : \"maHo\"\n")
            
            # 8. NhanKhau -> LichSuNhanKhau (maNhanKhau: một nhân khẩu có nhiều lịch sử)
            if self._find_class_in_all('NhanKhau') and self._find_class_in_all('LichSuNhanKhau'):
                f.write("    NhanKhau ||--o{ LichSuNhanKhau : \"maNhanKhau\"\n")
            
            # 8. Service -> Model (uses)
            for class_info in impl_classes + interface_classes:
                # Kiểm tra attributes để tìm model dependencies
                for attr in class_info.attributes:
                    attr_parts = attr.split()
                    if len(attr_parts) >= 2:
                        attr_type = attr_parts[1]  # Tên attribute
                        # Nếu là model class
                        model_target = self._find_class_in_all(attr_type)
                        if model_target and model_target in ['HoGiaDinh', 'NhanKhau', 'PhieuThu', 'Phong']:
                            f.write(f"    {class_info.name} --> {model_target} : uses\n")
            
            # 9. Service Implementation -> Service (dependency)
            for class_info in impl_classes:
                for attr in class_info.attributes:
                    attr_parts = attr.split()
                    if len(attr_parts) >= 2:
                        attr_type = attr_parts[1]
                        if 'Service' in attr_type and attr_type != class_info.name:
                            target = self._find_class_in_all(attr_type)
                            if target:
                                f.write(f"    {class_info.name} --> {target} : depends\n")
            
            f.write("```\n")
        
        return output_path

def main():
    """Hàm main"""
    # Đường dẫn đến thư mục src/main/java/com/bluemoon
    base_path = Path(__file__).parent / "src" / "main" / "java" / "com" / "bluemoon"
    
    if not base_path.exists():
        print(f"Không tìm thấy thư mục: {base_path}")
        print("Vui lòng chạy script từ thư mục gốc của project")
        return
    
    print("Đang quét các file Java...")
    parser = JavaParser(base_path)
    parser.scan_project()
    
    print(f"Đã tìm thấy {len(parser.classes)} lớp")
    
    print("Đang tạo biểu đồ Mermaid...")
    generator = MermaidGenerator(parser)
    
    # Tạo biểu đồ cho từng package
    generator.generate_all_diagrams("Class_Diagrams.md")
    print("✓ Đã tạo file: Class_Diagrams.md")
    
    # Tạo biểu đồ tổng hợp
    generator.generate_combined_diagram("Class_Diagram_Combined.md")
    print("✓ Đã tạo file: Class_Diagram_Combined.md")
    
    # Tạo biểu đồ riêng cho module Hộ gia đình
    generator.generate_household_diagram("Class_Diagram_HoGiaDinh.md")
    print("✓ Đã tạo file: Class_Diagram_HoGiaDinh.md")
    
    print("\nHoàn thành!")
    print("\nBạn có thể xem các biểu đồ bằng cách:")
    print("1. Mở file .md trong VS Code với extension Mermaid")
    print("2. Hoặc copy mã Mermaid vào https://mermaid.live/")

if __name__ == "__main__":
    main()

