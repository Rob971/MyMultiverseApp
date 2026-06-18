import { JWT } from "npm:google-auth-library@9.15.1";

type ServiceAccount = {
  project_id: string;
  client_email: string;
  private_key: string;
};

function isDeliverableAndroidToken(token: string, platform: string): boolean {
  if (platform !== "android") return false;
  if (token.startsWith("android-stub") || token.startsWith("ios-stub")) return false;
  return token.length > 0;
}

async function getFcmAccessToken(serviceAccount: ServiceAccount): Promise<string | null> {
  const client = new JWT({
    email: serviceAccount.client_email,
    key: serviceAccount.private_key,
    scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
  });
  const response = await client.getAccessToken();
  return response?.token ?? null;
}

export async function sendAndroidInvitePush(
  serviceAccountJson: string,
  deviceToken: string,
  title: string,
  body: string,
  data: Record<string, string>,
): Promise<boolean> {
  const serviceAccount = JSON.parse(serviceAccountJson) as ServiceAccount;
  const accessToken = await getFcmAccessToken(serviceAccount);
  if (!accessToken) {
    console.error("fcm_auth_failed");
    return false;
  }

  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token: deviceToken,
          notification: { title, body },
          data,
          android: { priority: "HIGH" },
        },
      }),
    },
  );

  if (!response.ok) {
    console.error("fcm_send_failed", await response.text());
    return false;
  }

  return true;
}

export { isDeliverableAndroidToken };
