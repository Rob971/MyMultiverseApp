import { SignJWT, importPKCS8 } from "npm:jose@5.9.6";

function isDeliverableIosToken(token: string, platform: string): boolean {
  if (platform !== "ios") return false;
  if (token.startsWith("ios-stub") || token.startsWith("android-stub")) return false;
  return /^[0-9a-fA-F]+$/.test(token) && token.length >= 64;
}

async function createApnsBearerToken(
  teamId: string,
  keyId: string,
  privateKeyP8: string,
): Promise<string | null> {
  try {
    const normalizedKey = privateKeyP8.includes("\\n")
      ? privateKeyP8.replace(/\\n/g, "\n")
      : privateKeyP8;
    const key = await importPKCS8(normalizedKey, "ES256");
    return await new SignJWT({})
      .setProtectedHeader({ alg: "ES256", kid: keyId })
      .setIssuer(teamId)
      .setIssuedAt(Math.floor(Date.now() / 1000))
      .sign(key);
  } catch (error) {
    console.error("apns_jwt_failed", error);
    return null;
  }
}

export async function sendIosInvitePush(
  teamId: string,
  keyId: string,
  privateKeyP8: string,
  bundleId: string,
  useSandbox: boolean,
  deviceToken: string,
  title: string,
  body: string,
  data: Record<string, string>,
): Promise<boolean> {
  const bearer = await createApnsBearerToken(teamId, keyId, privateKeyP8);
  if (!bearer) return false;

  const host = useSandbox
    ? "https://api.sandbox.push.apple.com"
    : "https://api.push.apple.com";
  const response = await fetch(`${host}/3/device/${deviceToken}`, {
    method: "POST",
    headers: {
      authorization: `bearer ${bearer}`,
      "apns-topic": bundleId,
      "apns-push-type": "alert",
      "apns-priority": "10",
      "content-type": "application/json",
    },
    body: JSON.stringify({
      aps: {
        alert: { title, body },
        sound: "default",
      },
      ...data,
    }),
  });

  if (response.status !== 200) {
    console.error("apns_send_failed", response.status, await response.text());
    return false;
  }

  return true;
}

export { isDeliverableIosToken };
