#include <dlfcn.h>
#include <stdio.h>

/* 14.0.0: Certificate loading crashed in the shipped ARMv7 FFmpeg/OpenSSL
 * library. Exercise its public TLS API without Java, JNI, or a remote server.
 * Keep the failing ABI in the release gate; do not disable certificate checks. */
int main(int argc, char **argv) {
    if (argc != 3) return 2;
    setbuf(stdout, NULL);
    void *library = dlopen(argv[1], RTLD_NOW | RTLD_LOCAL);
    if (!library) {
        fprintf(stderr, "dlopen: %s\n", dlerror());
        return 3;
    }
    const void *(*client_method)(void) = dlsym(library, "TLS_client_method");
    void *(*context_new)(const void *) = dlsym(library, "SSL_CTX_new");
    int (*load_ca)(void *, const char *, const char *) =
        dlsym(library, "SSL_CTX_load_verify_locations");
    void (*context_free)(void *) = dlsym(library, "SSL_CTX_free");
    const char *(*version)(int) = dlsym(library, "OpenSSL_version");
    if (!client_method || !context_new || !load_ca || !context_free || !version)
        return 4;
    puts(version(0));
    void *ctx = context_new(client_method());
    if (!ctx) return 5;
    puts("Loading fixture CA");
    int loaded = load_ca(ctx, argv[2], NULL);
    printf("Loaded=%d\n", loaded);
    context_free(ctx);
    return loaded == 1 ? 0 : 6;
}
