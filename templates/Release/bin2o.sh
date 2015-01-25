if [ -z "$KOS_ROMDISK_DIR" ]; then exit 0; fi

ROMDISKIMG=${CWD}/${KOS_ROMDISK_DIR}.img

${KOS_BASE}/utils/genromfs/genromfs -f ${ROMDISKIMG} -d ${CWD}/../${KOS_ROMDISK_DIR} -v -x .svn

TMPFILE1=${CWD}/script.ld
TMPFILE2=${CWD}/obja.o
TMPFILE3=${CWD}/objb.o

echo ".section .rodata; .align 2; " | ${GCC_BASE}/sh-elf/bin/sh-elf-as ${AFLAGS} -o ${TMPFILE3}
if [ $? -ne 0 ]; then exit 1; fi
echo "SECTIONS { .rodata : { _${KOS_ROMDISK_DIR} = .; *(.data); _${KOS_ROMDISK_DIR}_end = .; } }" > ${TMPFILE1}
${GCC_BASE}/sh-elf/bin/sh-elf-ld --no-warn-mismatch --format binary --oformat elf32-shl ${ROMDISKIMG} --format elf32-shl ${TMPFILE3} -o ${TMPFILE2} -r -EL -T ${TMPFILE1}
if [ $? -ne 0 ]; then exit 1; fi
${GCC_BASE}/sh-elf/bin/sh-elf-objcopy --set-section-flags .rodata=alloc,load,data,readonly $TMPFILE2 ${CWD}/romdisk
if [ $? -ne 0 ]; then exit 1; fi
rm -f ${TMPFILE1} ${TMPFILE2} ${TMPFILE3} ${ROMDISKIMG}